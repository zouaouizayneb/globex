import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccounteComponent } from './accounte.component';

describe('AccounteComponent', () => {
  let component: AccounteComponent;
  let fixture: ComponentFixture<AccounteComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccounteComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccounteComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
