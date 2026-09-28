import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnvioListComponent } from './envio-list.component';

describe('EnvioListComponent', () => {
  let component: EnvioListComponent;
  let fixture: ComponentFixture<EnvioListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvioListComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvioListComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
