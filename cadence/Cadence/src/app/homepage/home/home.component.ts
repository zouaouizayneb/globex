import { Component } from '@angular/core';
import { FirstComponent } from '../first/first.component';
import { SecondComponent } from "../second/second.component";
import { ThirdComponent } from '../third/third.component';
import { RatingComponent } from '../rating/rating.component';
import { ProductComponent } from '../product/product.component';
import { SupplierSliderComponent } from '../promotional-banner/promotional-banner.component';
import { BestSellingComponent } from '../best-selling/best-selling.component';


@Component({
  selector: 'app-home',
  imports: [FirstComponent,SecondComponent,ThirdComponent,RatingComponent ,ProductComponent, SupplierSliderComponent, BestSellingComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
