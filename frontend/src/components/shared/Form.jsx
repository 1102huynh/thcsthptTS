import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Form as FormProvider } from '@/components/ui/form';

/**
 * "Form wrapper chuẩn" per Tuần 2 Ngày 4 - a thin standardized layer over
 * React Hook Form + Zod so every page (StaffManagement, StudentManagement,
 * ... from Tuần 3 on) sets up a form the same way instead of repeating the
 * useForm/zodResolver/FormProvider/handleSubmit boilerplate each time.
 *
 * Usage:
 *   const form = useAppForm({ schema: staffSchema, defaultValues });
 *   <AppForm form={form} onSubmit={handleSave}>
 *     <TextField control={form.control} name="fullName" label="Họ tên" />
 *     ...
 *   </AppForm>
 * (TextField/SelectField/... live in FormFields.jsx, built on the same
 * `form.control` this hook returns.)
 */
export function useAppForm({ schema, defaultValues, ...options } = {}) {
  return useForm({
    resolver: schema ? zodResolver(schema) : undefined,
    defaultValues,
    mode: 'onBlur',
    ...options,
  });
}

/**
 * Wraps children in RHF's FormProvider (so FormField/useFormField work) and
 * a real <form> wired to `form.handleSubmit(onSubmit)`. `onSubmit` only
 * fires after Zod validation passes - invalid fields get their FormMessage
 * populated instead.
 */
export function AppForm({ form, onSubmit, children, className, id }) {
  return (
    <FormProvider {...form}>
      <form id={id} noValidate onSubmit={form.handleSubmit(onSubmit)} className={className}>
        {children}
      </form>
    </FormProvider>
  );
}

export default AppForm;
