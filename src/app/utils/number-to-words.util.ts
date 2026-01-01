/**
 * Converts a number to words in Indian numbering system
 * Supports numbers up to crores
 */
export function numberToWords(num: number): string {
  if (num === 0) return 'ZERO';
  
  // Remove decimals and work with integer
  const amount = Math.floor(num);
  
  const ones = ['', 'ONE', 'TWO', 'THREE', 'FOUR', 'FIVE', 'SIX', 'SEVEN', 'EIGHT', 'NINE'];
  const tens = ['', '', 'TWENTY', 'THIRTY', 'FORTY', 'FIFTY', 'SIXTY', 'SEVENTY', 'EIGHTY', 'NINETY'];
  const teens = ['TEN', 'ELEVEN', 'TWELVE', 'THIRTEEN', 'FOURTEEN', 'FIFTEEN', 'SIXTEEN', 'SEVENTEEN', 'EIGHTEEN', 'NINETEEN'];
  
  function convertLessThanThousand(n: number): string {
    if (n === 0) return '';
    
    let result = '';
    
    // Handle hundreds
    if (n >= 100) {
      result += ones[Math.floor(n / 100)] + ' HUNDRED ';
      n %= 100;
    }
    
    // Handle tens and ones
    if (n >= 20) {
      result += tens[Math.floor(n / 10)] + ' ';
      n %= 10;
    } else if (n >= 10) {
      result += teens[n - 10] + ' ';
      return result.trim();
    }
    
    if (n > 0) {
      result += ones[n] + ' ';
    }
    
    return result.trim();
  }
  
  let crore = Math.floor(amount / 10000000);
  let lakh = Math.floor((amount % 10000000) / 100000);
  let thousand = Math.floor((amount % 100000) / 1000);
  let remainder = amount % 1000;
  
  let words = '';
  
  if (crore > 0) {
    words += convertLessThanThousand(crore) + ' CRORE ';
  }
  
  if (lakh > 0) {
    words += convertLessThanThousand(lakh) + ' LAKH ';
  }
  
  if (thousand > 0) {
    words += convertLessThanThousand(thousand) + ' THOUSAND ';
  }
  
  if (remainder > 0) {
    words += convertLessThanThousand(remainder);
  }
  
  return words.trim();
}

/**
 * Converts amount to words with "RUPEES" prefix and "ONLY" suffix
 */
export function amountToWords(amount: number): string {
  if (!amount || amount === 0) return 'ZERO RUPEES ONLY';
  
  const words = numberToWords(amount);
  return `RUPEES ${words} ONLY`;
}
