declare module 'xterm' {
  export class Terminal {
    constructor(options?: any)
    open(element: HTMLElement): void
    write(text: string): void
    writeln(text: string): void
    onData(callback: (data: string) => void): void
    onResize(callback: (cols: number, rows: number) => void): void
    fit(): void
    dispose(): void
    resize(cols: number, rows: number): void
    clear(): void
  }
}

declare module 'xterm-addon-fit' {
  import { Terminal } from 'xterm'

  export class FitAddon {
    fit(): void
    dispose(): void
  }
}