import { Component } from '@angular/core';
import { InvoiceService, Invoice } from '../../services/invoice.service';

@Component({
  selector: 'app-invoice-view',
  templateUrl: './invoice-view.component.html',
  styleUrls: ['./invoice-view.component.scss']
})
export class InvoiceViewComponent {
  invoiceNo = '';
  lrNo = '';
  results: Invoice[] = [];
  selectedInvoice: Invoice | null = null;
  loading = false;

  constructor(private svc: InvoiceService) {}

  search() {
    this.selectedInvoice = null;
    this.results = [];
    this.loading = true;
    this.svc.searchInvoices(this.invoiceNo || undefined, this.lrNo || undefined).subscribe({
      next: res => { this.results = res; this.loading = false; },
      error: err => { console.error(err); this.loading = false; }
    });
  }

  view(id: number) {
    this.svc.getInvoice(id).subscribe({
      next: inv => { this.selectedInvoice = inv; },
      error: err => { console.error(err); }
    });
  }

  printPdf(id: number) {
    const url = this.svc.getInvoicePdfUrl(id);
    // open new tab to stream PDF inline
    window.open(url, '_blank');
  }
}
