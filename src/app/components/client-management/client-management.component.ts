import { Component, OnInit } from '@angular/core';
import { InvoiceService, Client } from '../../services/invoice.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-client-management',
  templateUrl: './client-management.component.html',
  styleUrls: ['./client-management.component.scss']
})
export class ClientManagementComponent implements OnInit {
  clients: Client[] = [];
  editingClient: Client | null = null;
  isNewClient = false;
  loading = false;

  newClient: Client = {
    name: '',
    address: '',
    gstNumber: '',
    phone: '',
    email: ''
  };

  constructor(private svc: InvoiceService, private snack: MatSnackBar) {}

  ngOnInit() {
    this.loadClients();
  }

  loadClients() {
    this.loading = true;
    this.svc.getAllClients().subscribe({
      next: (data) => {
        this.clients = data;
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.snack.open('Failed to load clients', 'Close', { duration: 3000 });
        this.loading = false;
      }
    });
  }

  startNewClient() {
    this.isNewClient = true;
    this.editingClient = null;
    this.newClient = {
      name: '',
      address: '',
      gstNumber: '',
      phone: '',
      email: ''
    };
  }

  editClient(client: Client) {
    this.isNewClient = false;
    this.editingClient = { ...client };
    this.newClient = { ...client };
  }

  saveClient() {
    if (!this.newClient.name || this.newClient.name.trim() === '') {
      this.snack.open('Client name is required', 'OK', { duration: 2000 });
      return;
    }

    if (this.isNewClient) {
      this.svc.createClient(this.newClient).subscribe({
        next: () => {
          this.snack.open('Client created successfully', 'Close', { duration: 2000 });
          this.loadClients();
          this.cancelEdit();
        },
        error: (err) => {
          console.error(err);
          this.snack.open('Failed to create client', 'Close', { duration: 3000 });
        }
      });
    } else if (this.editingClient && this.editingClient.id) {
      this.svc.updateClient(this.editingClient.id, this.newClient).subscribe({
        next: () => {
          this.snack.open('Client updated successfully', 'Close', { duration: 2000 });
          this.loadClients();
          this.cancelEdit();
        },
        error: (err) => {
          console.error(err);
          this.snack.open('Failed to update client', 'Close', { duration: 3000 });
        }
      });
    }
  }

  cancelEdit() {
    this.isNewClient = false;
    this.editingClient = null;
    this.newClient = {
      name: '',
      address: '',
      gstNumber: '',
      phone: '',
      email: ''
    };
  }
}
