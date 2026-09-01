import React from 'react';
import {
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormDescription,
  FormMessage,
} from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import DatePicker from './DatePicker';

/**
 * Reusable field components for AppForm (Form.jsx) - each wraps the
 * FormField/FormItem/FormLabel/FormControl/FormMessage boilerplate from
 * shadcn's ui/form.jsx around one input control, so a page's form body
 * reads as a flat list of fields instead of repeating that structure per
 * field. All take the same base props: `control` (from useAppForm), `name`
 * (must match a key in the Zod schema/defaultValues), `label`, optional
 * `description` (helper text) - plus whatever the underlying control needs.
 */

function FieldShell({ control, name, label, description, children }) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          {label && <FormLabel>{label}</FormLabel>}
          <FormControl>{children(field)}</FormControl>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export function TextField({ control, name, label, description, type = 'text', ...props }) {
  return (
    <FieldShell control={control} name={name} label={label} description={description}>
      {(field) => <Input type={type} {...field} {...props} />}
    </FieldShell>
  );
}

export function TextareaField({ control, name, label, description, ...props }) {
  return (
    <FieldShell control={control} name={name} label={label} description={description}>
      {(field) => <Textarea {...field} {...props} />}
    </FieldShell>
  );
}

export function SelectField({ control, name, label, description, options, placeholder = 'Chọn...' }) {
  // Doesn't go through FieldShell/FormControl the same way as the other
  // fields: shadcn's Select is a Radix Root (renders no DOM node of its
  // own) wrapping a Trigger + a portaled Content, so FormControl has to
  // wrap only the Trigger - wrapping the whole <Select> (as the generic
  // FieldShell does for every other field) sends FormControl's Slot-merged
  // id/aria-* props to a component with nothing to attach them to, which
  // silently breaks the label association (caught via Playwright's
  // getByLabel failing to find the field at all).
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => (
        <FormItem>
          {label && <FormLabel>{label}</FormLabel>}
          <Select value={field.value ?? ''} onValueChange={field.onChange}>
            <FormControl>
              <SelectTrigger>
                <SelectValue placeholder={placeholder} />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              {options.map((opt) => (
                <SelectItem key={opt.value} value={opt.value}>
                  {opt.label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          {description && <FormDescription>{description}</FormDescription>}
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

export function DateField({ control, name, label, description, placeholder }) {
  return (
    <FieldShell control={control} name={name} label={label} description={description}>
      {(field) => (
        <DatePicker value={field.value} onChange={field.onChange} placeholder={placeholder} />
      )}
    </FieldShell>
  );
}
