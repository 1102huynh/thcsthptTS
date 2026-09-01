import React, { useState } from 'react';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import { FiCalendar } from 'react-icons/fi';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';

const DATE_FORMAT = 'dd/MM/yyyy';

/**
 * Single-date picker (react-day-picker + shadcn Popover/Calendar), per
 * Tuần 2 Ngày 3. Vietnamese-localized (dd/MM/yyyy, date-fns `vi` locale for
 * month/weekday names) to match the rest of the app.
 *
 * Controlled: `value` is a Date or undefined/null, `onChange(date)` fires
 * on pick - the popover closes itself right after, matching how a native
 * `<input type="date">` behaves.
 */
function DatePicker({ value, onChange, placeholder = 'Chọn ngày', disabled, className }) {
  const [open, setOpen] = useState(false);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="outline"
          disabled={disabled}
          className={cn(
            'w-full justify-start text-left font-normal',
            !value && 'text-muted-foreground',
            className
          )}
        >
          <FiCalendar className="mr-2 h-4 w-4 shrink-0" />
          {value ? format(value, DATE_FORMAT, { locale: vi }) : placeholder}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="start">
        <Calendar
          mode="single"
          locale={vi}
          selected={value ?? undefined}
          onSelect={(date) => {
            onChange?.(date);
            setOpen(false);
          }}
          autoFocus
        />
      </PopoverContent>
    </Popover>
  );
}

export default DatePicker;
