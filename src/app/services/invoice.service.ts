import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface Client {
  id?: number | null;
  name?: string | null;
  address?: string | null;
  gstNumber?: string | null;
  phone?: string | null;
  email?: string | null;
}

export interface InvoiceItem {
  lrNo?: string;
  lrDate?: string;
  fromLocation?: string;
  toLocation?: string;
  goodsDescription?: string;
  packageType?: string;
  packageCount?: number;
  vehicleNumber?: string;
  vehicleType?: string;
  amount?: number;
}

export interface Invoice {
  id?: number;
  invoiceNo?: string;
  invoiceDate?: string;
  partyId?: number | null;
  partyName?: string;
  partyAddress?: string;
  partyGst?: string;
  haltingCharges?: number;
  loadingCharges?: number;
  unloadingCharges?: number;
  totalAmount?: number;
  amountInWords?: string;
  remarks?: string;
  items?: InvoiceItem[];
}

@Injectable({ providedIn: 'root' })
export class InvoiceService {
  base = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // invoice endpoints
  createInvoice(inv: Invoice): Observable<any> {
    return this.http.post(`${this.base}/invoices`, inv);
  }
  // add other invoice methods as needed (get/list)

  // client endpoints
  searchClients(q: string, limit = 10): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.base}/clients/search`, { params: { q: q || '', limit: String(limit) }});
  }

  createClient(c: Client): Observable<any> {
    return this.http.post(`${this.base}/clients`, c);
  }

  // invoice.service.ts additions
searchInvoices(invoiceNo?: string, lrNo?: string) {
    const params: any = {};
    if (invoiceNo) params.invoiceNo = invoiceNo;
    if (lrNo) params.lrNo = lrNo;
    return this.http.get<Invoice[]>(`${this.base}/invoices/search`, { params });
  }
  
  getInvoice(id: number) {
    return this.http.get<Invoice>(`${this.base}/invoices/${id}`);
  }
  
  // for PDF we will open in new tab using URL: /api/invoices/{id}/pdf
  getInvoicePdfUrl(id: number) {
    return `${this.base}/invoices/${id}/pdf`;
  }

  // for Excel we will download using URL: /api/invoices/{id}/excel
  getInvoiceExcelUrl(id: number) {
    return `${this.base}/invoices/${id}/excel`;
  }

  getNextInvoiceNumber() {
    return this.http.get(`${this.base}/invoices/next-number`, { responseType: 'text' });
  }

  updateInvoice(id: number, inv: Invoice): Observable<any> {
    return this.http.put(`${this.base}/invoices/${id}`, inv);
  }

  getAllClients(): Observable<Client[]> {
    return this.http.get<Client[]>(`${this.base}/clients`);
  }

  updateClient(id: number, client: Client): Observable<any> {
    return this.http.put(`${this.base}/clients/${id}`, client);
  }

  getReportPdfUrl(startDate: string, endDate: string): string {
    return `${this.base}/reports/invoices/pdf?startDate=${startDate}&endDate=${endDate}`;
  }

  getReportExcelUrl(startDate: string, endDate: string): string {
    return `${this.base}/reports/invoices/excel?startDate=${startDate}&endDate=${endDate}`;
  }
  
}
