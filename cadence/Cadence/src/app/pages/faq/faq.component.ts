import { Component } from '@angular/core';
import { CommonModule } from '@angular/common'

import { RouterModule } from '@angular/router';
@Component({
  selector: 'app-faq',
  imports: [CommonModule,RouterModule],
  templateUrl: './faq.component.html',
  styleUrl: './faq.component.css'
})
export class FaqComponent {
    // Define the list of FAQs with their respective questions and answers
    faqs = [
      {
        question: "Do I need to open an account in order to shop with you?",
        answer: "No, you don’t need to. You can make purchases and check out as a guest every time. However, by setting up an account with us, it will allow you to order without having to enter your details every time you shop with us. You can sign up right now, or you can first start shopping and create your account before you check out at the shopping cart page.",
        isOpen: false
      },
      {
        question: "How do I create an account?",
        answer: "Please click on 'Login/Register' followed by ‘Create An Account’ and fill in your personal particulars.",
        isOpen: false
      },
      {
        question: "How do I order?",
        answer: "Shop for the items you want and add them to your shopping cart. When you have finished, you can proceed to your shopping cart and check out. Check and ensure that all information is correct before confirming your purchases and payment.",
        isOpen: false
      },
      {
        question: "How do I pay for my orders?",
        answer: "We accept payments via Paypal and all major credit and debit cards such as Mastercard, VISA, and American Express.",
        isOpen: false
      },
      {
        question: "Can I amend and cancel my order?",
        answer: "Unfortunately, we are unable to cancel an order once it has been placed. This will allow us to pack your orders efficiently and minimize errors. It is advisable to check your order before placing it.",
        isOpen: false
      },
      {
        question: "I have a discount code, how can I use it?",
        answer: "Unfortunately, we are unable to cancel an order once it has been placed. This will allow us to pack your orders efficiently and minimize errors. It is advisable to check your order before placing it.",
        isOpen: false
      }
    ];
  
    // Function to toggle the FAQ visibility
    toggle(faq: { isOpen: boolean; }) {
      faq.isOpen = !faq.isOpen;
    }
  
}
