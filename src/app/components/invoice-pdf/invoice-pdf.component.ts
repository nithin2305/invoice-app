import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({ 
  selector: 'app-invoice-pdf', 
  templateUrl: './invoice-pdf.component.html',
  styleUrls: ['./invoice-pdf.component.scss']
})
export class InvoicePdfComponent implements OnInit {
  pdfUrl!: string;
  constructor(private route: ActivatedRoute) {}
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    // Use /api/invoices/{id}/pdf to match the service endpoint
    this.pdfUrl = `/api/invoices/${id}/pdf`;
  }
}
