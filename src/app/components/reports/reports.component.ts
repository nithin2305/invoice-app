import { Component } from '@angular/core';
import { InvoiceService } from '../../services/invoice.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.scss']
})
export class ReportsComponent {
  startDate = '';
  endDate = '';

  constructor(private svc: InvoiceService, private snack: MatSnackBar) {
    // Set default dates to current month
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.startDate = this.formatDate(firstDay);
    this.endDate = this.formatDate(today);
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  downloadPdf() {
    if (!this.startDate || !this.endDate) {
      this.snack.open('Please select both start and end dates', 'OK', { duration: 2000 });
      return;
    }

    const url = this.svc.getReportPdfUrl(this.startDate, this.endDate);
    window.open(url, '_blank');
  }

  downloadExcel() {
    if (!this.startDate || !this.endDate) {
      this.snack.open('Please select both start and end dates', 'OK', { duration: 2000 });
      return;
    }

    const url = this.svc.getReportExcelUrl(this.startDate, this.endDate);
    window.open(url, '_blank');
  }
}
