import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-logo',
  standalone: true,
  template: `
    <svg [attr.width]="size" [attr.height]="size" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <linearGradient [attr.id]="'gold-' + id" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0%" stop-color="#D4B876"/>
          <stop offset="100%" stop-color="#B08D45"/>
        </linearGradient>
      </defs>
      <rect width="64" height="64" rx="14" fill="#0A2540"/>
      <g fill="none" [attr.stroke]="'url(#gold-' + id + ')'"
         stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M14 18 H50 V24 H14 Z"/>
        <path d="M18 24 V50 H26 V24"/>
        <path d="M38 24 V50 H46 V24"/>
        <path d="M14 50 H50 V54 H14 Z"/>
        <path d="M28 24 V50 M36 24 V50"/>
      </g>
    </svg>
  `,
})
export class LogoComponent {
  @Input() size = 32;
  id = Math.random().toString(36).slice(2, 8);
}