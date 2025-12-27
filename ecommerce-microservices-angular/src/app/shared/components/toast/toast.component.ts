import { Component } from '@angular/core';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  template: `
    <div class="fixed top-24 right-5 z-50 flex flex-col gap-3">
      <div *ngFor="let toast of toastService.toasts$ | async" 
           class="min-w-[300px] p-4 rounded-xl shadow-lg transform transition-all duration-300 animate-slide-in"
           [ngClass]="{
               'bg-green-100 text-green-800 border-l-4 border-green-500': toast.type === 'success',
               'bg-red-100 text-red-800 border-l-4 border-red-500': toast.type === 'error',
               'bg-blue-100 text-blue-800 border-l-4 border-blue-500': toast.type === 'info'
           }">
        <div class="flex justify-between items-center">
            <span class="font-medium">{{ toast.message }}</span>
            <button (click)="toastService.remove(toast.id)" class="text-gray-500 hover:text-gray-700">
                <i class="bi bi-x-lg"></i>
            </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slide-in {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    .animate-slide-in {
        animation: slide-in 0.3s ease-out;
    }
  `]
})
export class ToastComponent {
  constructor(public toastService: ToastService) {}
}
