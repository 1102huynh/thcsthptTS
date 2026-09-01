// shadcn's generated version of this file imports useTheme from
// "next-themes" - this project has its own ThemeProvider (Tuần 1 Ngày 5,
// src/providers/ThemeProvider.jsx) instead, so swapped to that (same
// {theme} shape) and dropped next-themes from package.json rather than
// carry an unused, silently-wrong-default dependency.
import { Toaster as Sonner } from "sonner"
import { useTheme } from "@/providers/ThemeProvider"

const Toaster = ({
  ...props
}) => {
  const { theme } = useTheme()

  return (
    <Sonner
      theme={theme}
      className="toaster group"
      toastOptions={{
        classNames: {
          toast:
            "group toast group-[.toaster]:bg-background group-[.toaster]:text-foreground group-[.toaster]:border-border group-[.toaster]:shadow-lg",
          description: "group-[.toast]:text-muted-foreground",
          actionButton:
            "group-[.toast]:bg-primary group-[.toast]:text-primary-foreground",
          cancelButton:
            "group-[.toast]:bg-muted group-[.toast]:text-muted-foreground",
        },
      }}
      {...props} />
  );
}

export { Toaster }
