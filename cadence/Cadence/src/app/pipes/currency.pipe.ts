import { Pipe, PipeTransform } from '@angular/core';
import { CurrencyService } from '../services/currency.service';

@Pipe({
  name: 'appCurrency',
  standalone: true,
  pure: false // Important: this makes the pipe react to service changes without re-rendering the whole component
})
export class AppCurrencyPipe implements PipeTransform {
  constructor(private currencyService: CurrencyService) {}

  transform(priceInUSD: number): string {
    if (priceInUSD === null || priceInUSD === undefined) return '';
    return this.currencyService.formatPrice(priceInUSD);
  }
}
