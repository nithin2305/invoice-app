import { Component, OnInit, Input } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { InvoiceService, Invoice, InvoiceItem } from '../../services/invoice.service';

@Component({
  selector: 'app-invoice-print',
  templateUrl: './invoice-print.component.html',
  styleUrls: ['./invoice-print.component.scss']
})
export class InvoicePrintComponent implements OnInit {
  @Input() invoiceData?: Invoice;
  invoice?: Invoice;
  loading = false;
  copyType: 'DUPLICATE' | 'ORIGINAL' = 'DUPLICATE';

  // Company Details
  readonly companyName = 'SHRIRAM LOGISTICS';
  readonly companyAddress = 'No. 66/1, Mettu Street, Kaladipet Chennai- 600 019';
  readonly companyContact = 'Contact No : 044 - 4213 3684';
  readonly companyEmail = 'E-Mail : shriramlogics@gmail.com';
  readonly companyState = 'State: TamilNadu';
  readonly companyGstin = 'GSTIN: 33AJBPM6638G1ZA';
  readonly companyPan = 'PAN No: AJBPM6638G';

  // Bank Details
  readonly bankName = 'CANARA BANK';
  readonly bankAccount = '60151400000726';
  readonly bankBranch = 'Mylapore Branch';
  readonly bankIfsc = 'CNRB0016015';

  constructor(
    private route: ActivatedRoute,
    private invoiceService: InvoiceService
  ) {}

  ngOnInit(): void {
    if (this.invoiceData) {
      this.invoice = this.invoiceData;
    } else {
      const id = this.route.snapshot.paramMap.get('id');
      if (id) {
        this.loadInvoice(+id);
      } else {
        // Load demo data for testing
        this.loadDemoData();
      }
    }
  }

  loadDemoData(): void {
    this.invoice = {
      invoiceNo: '2902',
      invoiceDate: '2025-12-18',
      partyName: 'GERMAN POLYMERS AND COATINGS PVTE LIMITED',
      partyAddress: 'No. 18 Othivakkam SF No.82/1,88/6B,100/F, 1G,3,4,\nKumuzhi Vullage, Vandalur Taluk\nChengalpattu Dist -603202',
      partyGst: '33AABCG1253F1Z7',
      haltingCharges: 500,
      loadingCharges: 1000,
      unloadingCharges: 1500,
      totalAmount: 17000,
      amountInWords: 'RUPEES SEVENTEEN THOUSAND ONLY',
      items: [{
        lrNo: '3069',
        lrDate: '2025-12-18',
        fromLocation: 'CHENNAI',
        toLocation: 'SRIPERAMBADUR',
        goodsDescription: 'MODULE MOUNTING',
        packageType: 'AS PER INVOICE',
        vehicleNumber: 'TN01AB1234',
        vehicleType: '40FT',
        amount: 14000
      }]
    };
  }

  loadInvoice(id: number): void {
    this.loading = true;
    this.invoiceService.getInvoice(id).subscribe({
      next: (data) => {
        this.invoice = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading invoice:', err);
        this.loading = false;
      }
    });
  }

  print(): void {
    window.print();
  }

  toggleCopyType(): void {
    this.copyType = this.copyType === 'DUPLICATE' ? 'ORIGINAL' : 'DUPLICATE';
  }

  formatDate(dateStr: string | undefined): string {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    return `${day}.${month}.${year}`;
  }

  formatAmount(amount: number | undefined): string {
    if (!amount) return '0';
    return amount.toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }

  getItemsTotal(): number {
    if (!this.invoice?.items) return 0;
    return this.invoice.items.reduce((sum: number, item: InvoiceItem) => sum + (item.amount || 0), 0);
  }

  hasAdditionalCharges(): boolean {
    return !!(this.invoice?.haltingCharges || this.invoice?.loadingCharges || this.invoice?.unloadingCharges);
  }
}
