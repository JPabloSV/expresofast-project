import { ComponentFixture, TestBed } from '@angular/core/testing';
import { EnvioTrackingComponent } from './envio-tracking.component';

describe('EnvioTrackingComponent', () => {
  let component: EnvioTrackingComponent;
  let fixture: ComponentFixture<EnvioTrackingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnvioTrackingComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(EnvioTrackingComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
