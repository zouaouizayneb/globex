import { Routes } from '@angular/router';
import { HomeComponent } from './homepage/home/home.component'; 
import { AboutUsComponent } from './pages/about-us/about-us.component';
import { FaqComponent } from './pages/faq/faq.component';
import { ArticlepageComponent } from './pages/articlepage/articlepage.component';
import { CollectionsComponent } from './pages/collections/collections.component';
import { ListArticleComponent } from './article/list-article/list-article.component';
import { ProfilComponent } from './profil/profil.component';
import { ContactComponent } from './pages/contact/contact.component';
import { CreateaccComponent } from './profil/createacc/createacc.component';
import { AccounteComponent } from './pages/accounte/accounte.component';
import { WishlistComponent } from './pages/wishlist/wishlist.component';
import { ShippingComponent } from './pages/shipping/shipping.component';
import { CartComponent } from './pages/cart/cart.component';
import { LoginComponent } from './pages/login/login.component';
import { CheckoutComponent } from './pages/checkout/checkout.component';
import { ChatbotComponent } from './chatbot/chatbot.component';
import { PaymentResultComponent } from './pages/payment-result/payment-result.component';
import { adminRoutes } from './admin/admin.routes';
import { loginGuard } from './guards/login.guard';
import { adminGuard } from './guards/admin.guard';

export const appRoutes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'home', component: HomeComponent },
  { path: 'about-us', component: AboutUsComponent },
  { path: 'faq', component: FaqComponent },
  { path: 'articlepage', component: ArticlepageComponent },
  { path: 'collections', component: CollectionsComponent},
  { path: 'list-article/:category', component: ListArticleComponent},
  { path: 'profile', component: ProfilComponent },
  { path: 'contact' , component: ContactComponent},
  { path: 'createacc', component: CreateaccComponent},
  { path: 'account', component: AccounteComponent},
  { path: 'wishlist' , component: WishlistComponent},
  { path: 'shipping' , component: ShippingComponent},
  { path: 'cart', redirectTo: 'home', pathMatch: 'full' },
  { path: 'checkout', component: CheckoutComponent },
  { path: 'payment-result', component: PaymentResultComponent },
  { path: 'chatbot', component: ChatbotComponent },
  { path: 'admin', children: adminRoutes, canActivate: [adminGuard] }
];