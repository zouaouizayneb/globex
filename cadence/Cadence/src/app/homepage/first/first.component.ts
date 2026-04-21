import { Component, ViewChild } from '@angular/core';
import { RouterModule } from '@angular/router';


@Component({
  selector: 'app-first',
  imports: [RouterModule],
  templateUrl: './first.component.html',
  styleUrl: './first.component.css'
})
export class FirstComponent {
  @ViewChild('videoElement') videoElement: any;

  ngAfterViewInit() {
    // Vérifie si l'élément vidéo est chargé et applique 'muted' immédiatement
    if (this.videoElement) {
      const video = this.videoElement.nativeElement;

      // Assurer que la vidéo est muette avant de commencer
      video.muted = true;
      
      // Lance la vidéo en assurant qu'elle démarre muette
      video.play().then(() => {
        video.muted = true;
      }).catch((error: any) => {
        console.error("Erreur lors de l'activation de la lecture vidéo:", error);
      });
    }
  }
}
