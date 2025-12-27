# InvoiceApp

A logistics invoice application with Angular frontend and Spring Boot backend.

## 🚀 Deployment

- **Frontend**: Deployed on [Netlify](https://www.netlify.com/) 
- **Backend**: Ready for deployment on [Render.com](https://render.com/) (Free tier)

👉 **See [DEPLOYMENT.md](./DEPLOYMENT.md) for complete backend deployment instructions.**

## Project Structure

```
invoice-app/
├── src/                  # Angular frontend source code
├── backend/              # Spring Boot backend source code
│   └── src/main/java/com/invoice/app/
│       ├── controller/   # REST API controllers
│       ├── service/      # Business logic services
│       ├── repository/   # JPA repositories
│       ├── entity/       # JPA entities
│       ├── dto/          # Data transfer objects
│       └── config/       # Configuration classes
├── db/                   # Database scripts
│   ├── mysql/            # MySQL initialization scripts
│   └── h2/               # H2 initialization scripts
├── package.json          # Frontend dependencies
└── backend/pom.xml       # Backend dependencies
```

## Frontend (Angular)

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 16.1.6.

### Development server

Run `npm start` or `ng serve --proxy-config proxy.conf.json` for a dev server. Navigate to `http://localhost:4200/`. The application will automatically reload if you change any of the source files.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory.

### Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Backend (Spring Boot)

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Running the Backend

```bash
cd backend
mvn spring-boot:run
```

The backend will start on `http://localhost:8080`.

### Database

By default, the application uses H2 in-memory database. The H2 console is available at `http://localhost:8080/h2-console`.

#### Database Scripts

SQL scripts are available in the `db/` directory to manually create the database and tables:

- **MySQL (Production)**: `db/mysql/init.sql`
- **H2 (Development)**: `db/h2/init.sql`

##### MySQL Setup

1. Install MySQL Server and start it
2. Run the initialization script:
   ```bash
   mysql -u root -p < db/mysql/init.sql
   ```
3. Update `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/invoicedb
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
   ```

##### H2 Setup (Development)

For development, H2 tables are automatically created by JPA/Hibernate (`spring.jpa.hibernate.ddl-auto=update`). If you need to manually initialize the database, you can use the script at `db/h2/init.sql`.

#### Database Schema

The application has three main tables:

| Table | Primary Key | Description |
|-------|-------------|-------------|
| `clients` | `id` (BIGINT, AUTO_INCREMENT) | Stores client/party information |
| `invoices` | `id` (BIGINT, AUTO_INCREMENT) | Stores invoice header information |
| `invoice_items` | `id` (BIGINT, AUTO_INCREMENT) | Stores invoice line items (LR details) |

**Foreign Key Relationships:**
- `invoices.party_id` → `clients.id` (ON DELETE SET NULL)
- `invoice_items.invoice_id` → `invoices.id` (ON DELETE CASCADE)

### API Endpoints

#### Clients
- `POST /api/clients` - Create a new client
- `GET /api/clients/search?q={query}&limit={limit}` - Search clients by name, address, or GST number
- `GET /api/clients/{id}` - Get client by ID
- `GET /api/clients` - Get all clients

#### Invoices
- `POST /api/invoices` - Create a new invoice
- `GET /api/invoices/{id}` - Get invoice by ID
- `GET /api/invoices/search?invoiceNo={invoiceNo}&lrNo={lrNo}` - Search invoices
- `GET /api/invoices` - Get all invoices
- `GET /api/invoices/{id}/pdf` - Generate and download invoice PDF

#### Monthly Statements
- `GET /api/statements/monthly?year={year}&month={month}` - Get monthly statement data
- `GET /api/statements/monthly/pdf?year={year}&month={month}` - Generate and download monthly statement PDF

### Running Backend Tests

```bash
cd backend
mvn test
```

## Full Stack Development

1. Start the backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. Start the frontend (in a new terminal):
   ```bash
   npm start
   ```

3. Open `http://localhost:4200` in your browser

The frontend proxy configuration will forward API requests from `/api/*` to `http://localhost:8080`.

## Data Model

### Client
- `id` - Primary key
- `name` - Client name
- `address` - Client address
- `gstNumber` - GST registration number
- `phone` - Phone number
- `email` - Email address

### Invoice
- `id` - Primary key
- `invoiceNo` - Invoice number (unique)
- `invoiceDate` - Invoice date
- `partyId` - Reference to client
- `partyName`, `partyAddress`, `partyGst` - Party details
- `haltingCharges`, `loadingCharges`, `unloadingCharges` - Additional charges
- `totalAmount` - Total invoice amount
- `amountInWords` - Amount in words
- `remarks` - Invoice remarks
- `items` - List of invoice line items

### InvoiceItem (LR - Lorry Receipt)
- `id` - Primary key
- `lrNo` - LR number
- `lrDate` - LR date
- `fromLocation`, `toLocation` - Route information
- `goodsDescription` - Description of goods
- `packageType`, `packageCount` - Package details
- `vehicleNumber`, `vehicleType` - Vehicle details
- `amount` - Item amount
