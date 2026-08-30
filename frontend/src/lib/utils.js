import { clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

// Standard shadcn/ui helper - merges conditional className fragments (clsx)
// then dedupes conflicting Tailwind classes so the last one wins (twMerge),
// e.g. cn('px-2', condition && 'px-4') => 'px-4', not both.
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}
