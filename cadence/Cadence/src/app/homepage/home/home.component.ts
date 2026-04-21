import { Component } from '@angular/core';
import { FirstComponent } from '../first/first.component';
import { SecondComponent } from "../second/second.component";
import { ThirdComponent } from '../third/third.component';
import { RatingComponent } from '../rating/rating.component';
import { ProductComponent } from '../product/product.component';


@Component({
  selector: 'app-home',
  imports: [FirstComponent,SecondComponent,ThirdComponent,RatingComponent ,ProductComponent],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
