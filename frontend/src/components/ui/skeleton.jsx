import { cn } from "@/lib/utils"

// shadcn's stock primitive is `bg-muted`, but this theme's --muted (96.1%
// lightness) sits almost on top of --background/--card (white) - on an
// actual page the bars rendered with the right size/position but were all
// but invisible against their white card background (caught by screenshot,
// not just reading the CSS). `bg-foreground/10` is contrast-guaranteed by
// construction in both themes, since --foreground is always the
// high-contrast opposite of --background.
function Skeleton({ className, ...props }) {
  return (
    <div
      className={cn("animate-pulse rounded-md bg-foreground/10", className)}
      {...props}
    />
  )
}

export { Skeleton }
