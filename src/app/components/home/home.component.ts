import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { InvoiceService, Invoice } from '../../services/invoice.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {
  showSearchSection = false;
  searchInvoiceNo = '';
  searching = false;
  searchResult: Invoice | null = null;
  searchError = '';

  constructor(
    private router: Router,
    private invoiceService: InvoiceService
  ) {}

  navigateTo(route: string) {
    this.router.navigate([route]);
  }

  private resetSearchState() {
    this.searchResult = null;
    this.searchError = '';
    this.searchInvoiceNo = '';
  }

  openViewPrintInvoice() {
    this.showSearchSection = true;
    this.resetSearchState();
  }

  closeSearchSection() {
    this.showSearchSection = false;
    this.resetSearchState();
  }

  searchInvoice() {
    if (!this.searchInvoiceNo || this.searchInvoiceNo.trim() === '') {
      this.searchError = 'Please enter an invoice number';
      return;
    }

    this.searching = true;
    this.searchError = '';
    this.searchResult = null;

    this.invoiceService.searchInvoices(this.searchInvoiceNo.trim()).subscribe({
      next: (results) => {
        this.searching = false;
        if (results && results.length > 0) {
          this.searchResult = results[0];
        } else {
          this.searchError = 'No invoice found with that number';
        }
      },
      error: (err) => {
        console.error(err);
        this.searching = false;
        this.searchError = 'Search failed. Please try again.';
      }
    });
  }

  private openDownloadUrl(url: string) {
    window.open(url, '_blank');
  }

  downloadSearchResultPdf() {
    if (this.searchResult?.id) {
      this.openDownloadUrl(this.invoiceService.getInvoicePdfUrl(this.searchResult.id));
    }
  }

  downloadSearchResultExcel() {
    if (this.searchResult?.id) {
      this.openDownloadUrl(this.invoiceService.getInvoiceExcelUrl(this.searchResult.id));
    }
  }

  viewPrintPreview() {
    if (this.searchResult?.id) {
      this.router.navigate(['/invoice', this.searchResult.id, 'print']);
    }
  }
}
