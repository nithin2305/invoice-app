import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({ selector: 'app-invoice-pdf', templateUrl: './invoice-pdf.component.html' })
export class InvoicePdfComponent implements OnInit {
  pdfUrl!: string;
  constructor(private route: ActivatedRoute) {}
  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.pdfUrl = `${window.location.origin}/api/invoices/${id}/print`; // absolute to avoid dev-server route rewrite

    // this.pdfUrl = `/api/invoices/${id}/print`;
  }
}
