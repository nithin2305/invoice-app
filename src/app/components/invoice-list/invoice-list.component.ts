import { Component, OnInit } from '@angular/core';
import { InvoiceService, Invoice } from '../../services/invoice.service';

@Component({
  selector: 'app-invoice-list',
  templateUrl: './invoice-list.component.html',
  styleUrls: ['./invoice-list.component.scss']
})
export class InvoiceListComponent implements OnInit {
  columns = ['id', 'invoiceNo', 'invoiceDate', 'buyerName', 'totalAmount'];
  data: Invoice[] = [];
  constructor(private svc: InvoiceService) {}
  ngOnInit() { this.load(); }
  load() {
    // this.svc.list(100).subscribe(list => this.data = list);
  }
}
