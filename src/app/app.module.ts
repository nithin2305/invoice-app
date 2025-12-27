import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterModule, Routes } from '@angular/router';
import { CommonModule } from '@angular/common';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatTableModule } from '@angular/material/table';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { AppComponent } from './app.component';
import { InvoiceFormComponent } from './components/invoice-form/invoice-form.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { InvoiceListComponent } from './components/invoice-list/invoice-list.component';
import { InvoiceViewComponent } from './components/invoice-view/invoice-view.component';
import { InvoicePdfComponent } from './components/invoice-pdf/invoice-pdf.component';
import { SafeUrlPipe } from './pipes/safe-url.pipe';

const routes: Routes = [
  { path: '', component: InvoiceFormComponent }, // default route
  { path: 'invoices', component: InvoiceViewComponent },     // list/search page
  { path: 'invoice/:id/pdf', component: InvoicePdfComponent }, // PDF viewer route
  { path: '**', redirectTo: '' } // fallback
];
@NgModule({
  declarations: [
    AppComponent,
    InvoiceFormComponent,
    InvoiceListComponent,
    InvoiceViewComponent,
    InvoicePdfComponent,
    SafeUrlPipe
  ],
  imports: [
    CommonModule,
    BrowserModule,
        FormsModule ,
        HttpClientModule,
        BrowserAnimationsModule,
        MatToolbarModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatCardModule,
        MatTableModule,
        MatDividerModule,
        MatListModule,
        MatSnackBarModule,
        MatAutocompleteModule,
        RouterModule.forRoot(routes)

  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
