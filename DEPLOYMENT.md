# Backend Deployment Guide

This guide explains how to deploy the Invoice Application backend on **Render.com** (free tier) which is perfect for low-usage applications (up to 750 hours/month free).

## Why Render.com?

- **Free Tier**: 750 hours/month of free compute time (spins down after 15 minutes of inactivity)
- **Free PostgreSQL**: 90-day data retention on free tier
- **Easy Setup**: One-click deployment from GitHub
- **Auto-Deploy**: Automatic deployments when you push to GitHub
- **No Credit Card Required**: For free tier services

## Prerequisites

1. A GitHub account with this repository
2. A Render.com account (sign up at https://render.com)

## Deployment Steps

### Option 1: One-Click Deploy (Recommended)

1. **Sign up/Login to Render.com**
   - Go to https://render.com
   - Sign up using your GitHub account

2. **Create New Web Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Select the `invoice-app` repository

3. **Configure the Service**
   - **Name**: `invoice-backend`
   - **Region**: Choose nearest to your users
   - **Branch**: `main` (or your deployment branch)
   - **Root Directory**: `backend`
   - **Runtime**: Docker
   - **Instance Type**: Free

4. **Environment Variables**
   Add these environment variables:
   ```
   SPRING_PROFILES_ACTIVE=production
   JAVA_OPTS=-Xmx256m -Xms128m
   ```

5. **Create PostgreSQL Database**
   - Click "New +" → "PostgreSQL"
   - **Name**: `invoice-db`
   - **Instance Type**: Free
   - Copy the Internal Database URL

6. **Link Database to Web Service**
   - Go to your web service → Environment
   - Add: `DATABASE_URL` = (paste the Internal Database URL)

7. **Deploy**
   - Click "Manual Deploy" → "Deploy latest commit"

### Option 2: Using render.yaml (Blueprint)

The repository includes a `render.yaml` file for automated deployment:

1. Go to Render Dashboard
2. Click "New +" → "Blueprint"
3. Connect your repository
4. Render will auto-detect `render.yaml` and create all services

## Post-Deployment

### Get Your Backend URL

After deployment, your backend will be available at:
```
https://invoice-backend-xxxx.onrender.com
```

(Replace `xxxx` with your unique identifier)

### Update Frontend Configuration

1. Go to your Angular frontend code
2. Edit `src/environments/environment.prod.ts`
3. Update the `apiUrl` with your Render backend URL:
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://invoice-backend-xxxx.onrender.com/api'
   };
   ```
4. Rebuild and redeploy to Netlify

### Test the Deployment

1. Visit `https://your-backend-url.onrender.com/api/health`
2. You should see: `{"status":"UP","service":"invoice-backend"}`

## Important Notes

### Free Tier Limitations

1. **Spin Down**: Free services spin down after 15 minutes of inactivity. First request after spin-down may take 30-60 seconds.

2. **Database Retention**: Free PostgreSQL databases have 90-day data retention. Set a reminder to export data if needed.

3. **Compute Hours**: 750 hours/month. For 10 hours/month usage, this is more than sufficient.

### Cost Optimization Tips

1. The app spins down automatically when not in use
2. No additional cost for occasional usage
3. Consider upgrading only if you need always-on availability

## Troubleshooting

### Service Not Starting

1. Check logs in Render Dashboard → Logs
2. Verify environment variables are set correctly
3. Ensure DATABASE_URL is properly formatted

### CORS Issues

The backend already has CORS configured to allow all origins. If you still face issues:
1. Check browser console for specific error messages
2. Verify the API URL in your frontend matches the backend URL

### Database Connection Issues

1. Verify DATABASE_URL format: `postgres://user:password@host:port/database`
2. Check if the database is running (Dashboard → PostgreSQL → Status)

## Alternative Free Options

If Render doesn't suit your needs, consider:

1. **Railway.app**: Similar to Render, offers $5/month free credit
2. **Fly.io**: Free tier with 3 shared VMs
3. **Google Cloud Run**: Generous free tier with pay-as-you-go

## Support

For issues specific to this application, please open a GitHub issue in the repository.
