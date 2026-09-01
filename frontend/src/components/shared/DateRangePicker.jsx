import React, { useState } from 'react';
import { format } from 'date-fns';
import { vi } from 'date-fns/locale';
import { FiCalendar } from 'react-icons/fi';
import { cn } from '@/lib/utils';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';

const DATE_FORMAT = 'dd/MM/yyyy';

function formatRange(range) {
  if (!range?.from) return null;
  if (!range.to) return format(range.from, DATE_FORMAT, { locale: vi });
  return `${format(range.from, DATE_FORMAT, { locale: vi })} – ${format(range.to, DATE_FORMAT, { locale: vi })}`;
}

/**
 * Date-range picker, sibling to DatePicker.jsx (same styling/localization) -
 * per Tuần 2 Ngày 3. Controlled: `value` is `{ from, to }` (either may be
 * undefined), `onChange(range)` fires on every pick.
 *
 * Unlike DatePicker, this does NOT auto-close on selection: react-day-picker
 * sets `{ from: day, to: day }` (both ends equal) on the very *first* click
 * of a range, not `{ from: day, to: undefined }` - confirmed live, not
 * assumed - so a "close once both ends are set" rule would close the
 * popover after one click and make picking an actual multi-day range
 * impossible. Closing on outside click (Radix Popover's default) is the
 * same pattern most range pickers use (Airbnb, Google Flights, ...).
 */
function DateRangePicker({ value, onChange, placeholder = 'Chọn khoảng ngày', disabled, className }) {
  const [open, setOpen] = useState(false);
  const label = formatRange(value);

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button
          type="button"
          variant="outline"
          disabled={disabled}
          className={cn(
            'w-full justify-start text-left font-normal',
            !label && 'text-muted-foreground',
            className
          )}
        >
          <FiCalendar className="mr-2 h-4 w-4 shrink-0" />
          {label ?? placeholder}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-auto p-0" align="start">
        <Calendar
          mode="range"
          locale={vi}
          numberOfMonths={2}
          selected={value}
          onSelect={onChange}
          autoFocus
        />
      </PopoverContent>
    </Popover>
  );
}

export default DateRangePicker;
