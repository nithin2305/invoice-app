import { Component, OnInit, OnDestroy } from '@angular/core';
import { InvoiceService, Invoice, InvoiceItem, Client } from '../../services/invoice.service';
import { Subject, Observable, of, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError } from 'rxjs/operators';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-invoice-form',
  templateUrl: './invoice-form.component.html',
  styleUrls: ['./invoice-form.component.scss']
})
export class InvoiceFormComponent implements OnInit, OnDestroy {
  invoice: Invoice = {
    invoiceNo: '',
    invoiceDate: '',
    partyId: null,
    partyName: '',
    partyAddress: '',
    partyGst: '',
    haltingCharges: 0,
    loadingCharges: 0,
    unloadingCharges: 0,
    totalAmount: 0,
    amountInWords: '',
    remarks: '',
    items: [{ lrNo: '', lrDate: '', fromLocation: '', toLocation: '', goodsDescription: '', packageType: '', packageCount: 0, vehicleNumber: '', vehicleType: '', amount: 0 }]
  };

  // autocomplete streams
  private partySearch$ = new Subject<string>();
  partyOptions$: Observable<Client[]> = of([]);
  private subs = new Subscription();
  saving = false;
  searching = false;

  constructor(private svc: InvoiceService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.partyOptions$ = this.partySearch$.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap(q => {
        if (!q || q.trim().length < 1) return of([]);
        this.searching = true;
        return this.svc.searchClients(q.trim(), 10).pipe(
          catchError(() => of([]))
        );
      }),
    );

    // optional: update searching flag when results arrive
    const p = this.partyOptions$.subscribe(() => this.searching = false);
    this.subs.add(p);
  }

  ngOnDestroy() {
    this.subs.unsubscribe();
  }

  onPartyInput(v: string) {
    this.partySearch$.next(v);
  }

  onClientSelected(client: Client) {
    // mat-autocomplete optionSelected will pass the Client object
    this.invoice.partyId = client.id ?? null;
    this.invoice.partyName = client.name ?? '';
    this.invoice.partyAddress = client.address ?? '';
    this.invoice.partyGst = client.gstNumber ?? '';
  }

  // If user types a name and wants to create a client on the fly
  createClientFromInput() {
    if (!this.invoice.partyName || this.invoice.partyName.trim().length < 1) {
      this.snack.open('Enter party name first', 'ok', { duration: 2000 });
      return;
    }
    const c: Client = {
      name: this.invoice.partyName,
      address: this.invoice.partyAddress,
      gstNumber: this.invoice.partyGst
    };
    this.svc.createClient(c).subscribe({
      next: (res: any) => {
        this.invoice.partyId = res.id;
        this.snack.open('Client created ✓', 'close', { duration: 2000 });
      },
      error: (err) => {
        console.error(err);
        this.snack.open('Create failed', 'retry', { duration: 2000 });
      }
    });
  }

  addItem() {
    this.invoice.items = this.invoice.items || [];
    this.invoice.items.push({ lrNo: '', lrDate: '', fromLocation: '', toLocation: '', goodsDescription: '', packageType: '', packageCount: 0, vehicleNumber: '', vehicleType: '', amount: 0 });
  }

  removeItem(i: number) {
    if (!this.invoice.items) return;
    this.invoice.items.splice(i, 1);
  }

  get itemsTotal(): number {
    if (!this.invoice.items) return 0;
    return this.invoice.items.reduce((s, it) => s + (Number(it.amount || 0)), 0);
  }

  get grandTotal(): number {
    const h = Number(this.invoice.haltingCharges || 0);
    const l = Number(this.invoice.loadingCharges || 0);
    const u = Number(this.invoice.unloadingCharges || 0);
    return this.itemsTotal + h + l + u;
  }

  syncTotalBeforeSave() {
    // populate totalAmount on payload
    this.invoice.totalAmount = this.grandTotal;
  }

  save() {
    // basic validation
    if (!this.invoice.invoiceNo || this.invoice.invoiceNo.trim() === '') {
      this.snack.open('Invoice no required', 'ok', { duration: 2000 });
      return;
    }
    if (!this.invoice.items || this.invoice.items.length === 0) {
      this.snack.open('Add at least one LR / item', 'ok', { duration: 2000 });
      return;
    }
    this.syncTotalBeforeSave();
    this.saving = true;
    this.svc.createInvoice(this.invoice).subscribe({
      next: (res: any) => {
        this.saving = false;
        this.snack.open('Invoice saved (id: ' + res.id + ')', 'close', { duration: 3000 });
        // reset form but keep one empty row
        this.resetForm();
      },
      error: (err) => {
        console.error(err);
        this.saving = false;
        this.snack.open('Save failed', 'retry', { duration: 3000 });
      }
    });
  }

  resetForm() {
    this.invoice = {
      invoiceNo: '',
      invoiceDate: '',
      partyId: null,
      partyName: '',
      partyAddress: '',
      partyGst: '',
      haltingCharges: 0,
      loadingCharges: 0,
      unloadingCharges: 0,
      totalAmount: 0,
      amountInWords: '',
      remarks: '',
      items: [{ lrNo: '', lrDate: '', fromLocation: '', toLocation: '', goodsDescription: '', packageType: '', packageCount: 0, vehicleNumber: '', vehicleType: '', amount: 0 }]
    };
  }
}
