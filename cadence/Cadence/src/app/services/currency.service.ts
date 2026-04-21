import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private selectedCurrencySubject = new BehaviorSubject<string>('USD');
  private selectedCurrencyRateSubject = new BehaviorSubject<number>(1);
  private selectedCurrencySymbolSubject = new BehaviorSubject<string>('$');

  private exchangeRates: { [key: string]: number } = {
    'USD': 1,
    'AUD': 1.53,
    'CAD': 1.36,
    'GBP': 0.79,
    'EUR': 0.92,
    'TND': 3.12
  };

  private currencySymbols: { [key: string]: string } = {
    'USD': '$',
    'AUD': '$',
    'CAD': '$',
    'GBP': '£',
    'EUR': '€',
    'TND': 'د.ت'
  };

  constructor() {
    this.loadSavedCurrency();
  }

  private loadSavedCurrency() {
    const savedCurrency = localStorage.getItem('selectedCurrency');
    if (savedCurrency && this.exchangeRates[savedCurrency]) {
      this.selectedCurrencySubject.next(savedCurrency);
      this.selectedCurrencyRateSubject.next(this.exchangeRates[savedCurrency]);
      this.selectedCurrencySymbolSubject.next(this.currencySymbols[savedCurrency]);
    }
  }

  setCurrency(currencyCode: string) {
    if (this.exchangeRates[currencyCode]) {
      this.selectedCurrencySubject.next(currencyCode);
      this.selectedCurrencyRateSubject.next(this.exchangeRates[currencyCode]);
      this.selectedCurrencySymbolSubject.next(this.currencySymbols[currencyCode]);
      
      localStorage.setItem('selectedCurrency', currencyCode);
      localStorage.setItem('currencyRate', this.exchangeRates[currencyCode].toString());
    }
  }

  getSelectedCurrency(): Observable<string> {
    return this.selectedCurrencySubject.asObservable();
  }

  getSelectedCurrencyRate(): Observable<number> {
    return this.selectedCurrencyRateSubject.asObservable();
  }

  getSelectedCurrencySymbol(): Observable<string> {
    return this.selectedCurrencySymbolSubject.asObservable();
  }

  getCurrentCurrency(): string {
    return this.selectedCurrencySubject.value;
  }

  getCurrentRate(): number {
    return this.selectedCurrencyRateSubject.value;
  }

  getCurrentSymbol(): string {
    return this.selectedCurrencySymbolSubject.value;
  }

  convertPrice(priceInUSD: number): number {
    const rate = this.getCurrentRate();
    return Math.round(priceInUSD * rate * 100) / 100; 
  }

  formatPrice(priceInUSD: number): string {
    const convertedPrice = this.convertPrice(priceInUSD);
    const symbol = this.getCurrentSymbol();
    return `${symbol}${convertedPrice.toFixed(2)}`;
  }

  async fetchExchangeRates(): Promise<void> {
    try {
      const response = await fetch('https://api.exchangerate-api.com/v4/latest/USD');
      const data = await response.json();
      
      if (data.rates) {
        this.exchangeRates = {
          'USD': 1,
          'AUD': data.rates.AUD || 1.53,
          'CAD': data.rates.CAD || 1.36,
          'GBP': data.rates.GBP || 0.79,
          'EUR': data.rates.EUR || 0.92,
          'TND': data.rates.TND || 3.12
        };
        
        const currentCurrency = this.getCurrentCurrency();
        this.selectedCurrencyRateSubject.next(this.exchangeRates[currentCurrency]);
      }
    } catch (error) {
      console.error('Failed to fetch exchange rates:', error);
    }
  }
}
