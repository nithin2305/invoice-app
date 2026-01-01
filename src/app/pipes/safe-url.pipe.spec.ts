import { SafeUrlPipe } from './safe-url.pipe';
import { DomSanitizer } from '@angular/platform-browser';

describe('SafeUrlPipe', () => {
  it('create an instance', () => {
    const sanitizer = jasmine.createSpyObj('DomSanitizer', ['bypassSecurityTrustResourceUrl']);
    const pipe = new SafeUrlPipe(sanitizer as DomSanitizer);
    expect(pipe).toBeTruthy();
  });
});
