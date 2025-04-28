// src/app/not-found/not-found.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="min-h-screen flex items-center justify-center bg-gray-100 px-4">
      <div
        class="max-w-md w-full text-center bg-white shadow-2xl rounded-2xl p-8 animate-fadeInUp"
      >
        <!-- Highway Exit Icon -->
        <div class="text-6xl mb-4 text-red-500 animate-pulse">🚧</div>

        <!-- Title -->
        <h1 class="text-3xl font-bold text-red-600 mb-2">Ups! Wrong Path</h1>

        <!-- Description -->
        <p class="text-gray-600 mb-6">
          Looks like you took the wrong exit. This page doesn't exist or isn't available.
        </p>

        <!-- Go Back Button -->
        <a
          routerLink="/"
          class="inline-block bg-blue-600 text-white px-6 py-2 rounded-md hover:bg-blue-700 transition"
        >
          Back to Home
        </a>
      </div>
    </div>
  `,
  styles: [`
    @keyframes fadeInUp {
      0% {
        opacity: 0;
        transform: translateY(30px);
      }
      100% {
        opacity: 1;
        transform: translateY(0);
      }
    }

    .animate-fadeInUp {
      animation: fadeInUp 0.6s ease-out both;
    }
  `],
})
export class NotFoundComponent {}
