import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
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

  isEditMode = false;
  editInvoiceId: number | null = null;
  
  // For modify mode
  searchInvoiceNo = '';
  searchingInvoice = false;

  // For post-save download options
  savedInvoiceId: number | null = null;
  showDownloadOptions = false;

  // autocomplete streams
  private partySearch$ = new Subject<string>();
  partyOptions$: Observable<Client[]> = of([]);
  private subs = new Subscription();
  saving = false;
  searching = false;

  constructor(
    private svc: InvoiceService, 
    private snack: MatSnackBar,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    // Check if we're in edit or new mode based on route
    this.route.url.subscribe(segments => {
      const path = segments.map(s => s.path).join('/');
      this.isEditMode = path.includes('edit');
      
      if (!this.isEditMode) {
        // New invoice mode - set system date and get next invoice number
        this.setSystemDate();
        this.loadNextInvoiceNumber();
      }
    });

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

  setSystemDate() {
    const today = new Date();
    this.invoice.invoiceDate = today.toISOString().split('T')[0];
  }

  loadNextInvoiceNumber() {
    this.svc.getNextInvoiceNumber().subscribe({
      next: (nextNo) => {
        this.invoice.invoiceNo = nextNo;
      },
      error: (err) => {
        console.error('Failed to get next invoice number', err);
        this.invoice.invoiceNo = 'INV001';
      }
    });
  }

  searchInvoiceForEdit() {
    if (!this.searchInvoiceNo || this.searchInvoiceNo.trim() === '') {
      this.snack.open('Please enter invoice number', 'OK', { duration: 2000 });
      return;
    }

    this.searchingInvoice = true;
    this.svc.searchInvoices(this.searchInvoiceNo.trim()).subscribe({
      next: (results) => {
        this.searchingInvoice = false;
        if (results && results.length > 0) {
          const inv = results[0];
          if (inv.id) {
            this.loadInvoiceForEdit(inv.id);
          }
        } else {
          this.snack.open('Invoice not found', 'OK', { duration: 2000 });
        }
      },
      error: (err) => {
        console.error(err);
        this.searchingInvoice = false;
        this.snack.open('Search failed', 'OK', { duration: 2000 });
      }
    });
  }

  loadInvoiceForEdit(id: number) {
    this.svc.getInvoice(id).subscribe({
      next: (inv) => {
        this.editInvoiceId = id;
        this.invoice = { ...inv };
        if (!this.invoice.items || this.invoice.items.length === 0) {
          this.invoice.items = [{ lrNo: '', lrDate: '', fromLocation: '', toLocation: '', goodsDescription: '', packageType: '', packageCount: 0, vehicleNumber: '', vehicleType: '', amount: 0 }];
        }
        this.snack.open('Invoice loaded for editing', 'Close', { duration: 2000 });
      },
      error: (err) => {
        console.error(err);
        this.snack.open('Failed to load invoice', 'Close', { duration: 3000 });
      }
    });
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

    const saveObs = this.editInvoiceId 
      ? this.svc.updateInvoice(this.editInvoiceId, this.invoice)
      : this.svc.createInvoice(this.invoice);

    saveObs.subscribe({
      next: (res: any) => {
        this.saving = false;
        const action = this.editInvoiceId ? 'updated' : 'saved';
        this.snack.open(`Invoice ${action} (id: ${res.id})`, 'close', { duration: 3000 });
        
        // Show print options popup
        this.showPrintOptions(res.id);
      },
      error: (err) => {
        console.error(err);
        this.saving = false;
        this.snack.open('Save failed', 'retry', { duration: 3000 });
      }
    });
  }

  showPrintOptions(invoiceId: number) {
    // Store saved invoice ID and show download options
    this.savedInvoiceId = invoiceId;
    this.showDownloadOptions = true;
    
    // Show snackbar notification
    this.snack.open('Invoice saved successfully! Choose a download format below.', 'Close', {
      duration: 3000,
      horizontalPosition: 'center',
      verticalPosition: 'top'
    });
  }

  private openDownloadUrl(url: string) {
    window.open(url, '_blank');
  }

  downloadPdf() {
    if (this.savedInvoiceId) {
      this.openDownloadUrl(this.svc.getInvoicePdfUrl(this.savedInvoiceId));
    }
  }

  downloadExcel() {
    if (this.savedInvoiceId) {
      this.openDownloadUrl(this.svc.getInvoiceExcelUrl(this.savedInvoiceId));
    }
  }

  viewPrintPreview() {
    if (this.savedInvoiceId) {
      this.router.navigate(['/invoice', this.savedInvoiceId, 'print']);
    }
  }

  private resetDownloadState() {
    this.showDownloadOptions = false;
    this.savedInvoiceId = null;
  }

  createNewInvoice() {
    this.resetDownloadState();
    this.resetForm();
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
    this.editInvoiceId = null;
    this.searchInvoiceNo = '';
    
    // Reload defaults for new invoice
    if (!this.isEditMode) {
      this.setSystemDate();
      this.loadNextInvoiceNumber();
    }
  }
}
