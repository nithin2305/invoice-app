import { numberToWords, amountToWords } from './number-to-words.util';

describe('Number to Words Utility', () => {
  describe('numberToWords', () => {
    it('should convert 0 to ZERO', () => {
      expect(numberToWords(0)).toBe('ZERO');
    });

    it('should convert single digits', () => {
      expect(numberToWords(1)).toBe('ONE');
      expect(numberToWords(5)).toBe('FIVE');
      expect(numberToWords(9)).toBe('NINE');
    });

    it('should convert teens', () => {
      expect(numberToWords(10)).toBe('TEN');
      expect(numberToWords(15)).toBe('FIFTEEN');
      expect(numberToWords(19)).toBe('NINETEEN');
    });

    it('should convert tens', () => {
      expect(numberToWords(20)).toBe('TWENTY');
      expect(numberToWords(50)).toBe('FIFTY');
      expect(numberToWords(90)).toBe('NINETY');
    });

    it('should convert hundreds', () => {
      expect(numberToWords(100)).toBe('ONE HUNDRED');
      expect(numberToWords(500)).toBe('FIVE HUNDRED');
      expect(numberToWords(999)).toBe('NINE HUNDRED NINETY NINE');
    });

    it('should convert thousands', () => {
      expect(numberToWords(1000)).toBe('ONE THOUSAND');
      expect(numberToWords(5000)).toBe('FIVE THOUSAND');
      expect(numberToWords(14000)).toBe('FOURTEEN THOUSAND');
    });

    it('should convert lakhs', () => {
      expect(numberToWords(100000)).toBe('ONE LAKH');
      expect(numberToWords(500000)).toBe('FIVE LAKH');
    });

    it('should convert crores', () => {
      expect(numberToWords(10000000)).toBe('ONE CRORE');
      expect(numberToWords(50000000)).toBe('FIVE CRORE');
    });

    it('should convert complex numbers', () => {
      expect(numberToWords(17000)).toBe('SEVENTEEN THOUSAND');
      expect(numberToWords(123456)).toBe('ONE LAKH TWENTY THREE THOUSAND FOUR HUNDRED FIFTY SIX');
    });
  });

  describe('amountToWords', () => {
    it('should convert 0 to ZERO RUPEES ONLY', () => {
      expect(amountToWords(0)).toBe('ZERO RUPEES ONLY');
    });

    it('should add RUPEES prefix and ONLY suffix', () => {
      expect(amountToWords(17000)).toBe('RUPEES SEVENTEEN THOUSAND ONLY');
    });

    it('should handle large amounts', () => {
      expect(amountToWords(123456)).toBe('RUPEES ONE LAKH TWENTY THREE THOUSAND FOUR HUNDRED FIFTY SIX ONLY');
    });
  });
});
