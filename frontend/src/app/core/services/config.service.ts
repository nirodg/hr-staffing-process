// src/app/core/services/config.service.ts
import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ConfigService {
  readonly siteName = 'Staffing Portal';
  readonly companyName = 'Your Company';
}
