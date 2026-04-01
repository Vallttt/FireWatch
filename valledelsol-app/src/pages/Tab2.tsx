import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonCard,
  IonCardHeader,
  IonCardTitle,
  IonCardContent,
  IonItem,
  IonLabel,
  IonInput,
  IonTextarea,
  IonButton,
  IonDatetime
} from '@ionic/react';
import './Tab2.css';

const Tab2: React.FC = () => {
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar color="warning">
          <IonTitle>Reportar Incidente</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent fullscreen color="light">
        <IonCard className="reporte-card">
          <IonCardHeader>
            <IonCardTitle>Nuevo Reporte de Incendio</IonCardTitle>
          </IonCardHeader>

          <IonCardContent>
            <IonItem className="form-item">
              <IonLabel position="stacked">
                Testigo / Bombero a Cargo
              </IonLabel>
              <IonInput placeholder="Nombre o Placa..." />
            </IonItem>

            <IonItem className="form-item">
              <IonLabel position="stacked">
                Descripción del Incidente
              </IonLabel>
              <IonTextarea
                rows={3}
                placeholder="Pasto seco quemándose, estructura en llamas..."
              />
            </IonItem>

            <IonItem className="form-item">
              <IonLabel position="stacked">
                Estado Inicial
              </IonLabel>
              <IonInput placeholder="Detectado, En Combate, o Descontrolado" />
            </IonItem>

            <IonButton
              expand="block"
              color="warning"
              className="btn-enviar"
            >
              ENVIAR REPORTE AL CENTRO DE CONTROL
            </IonButton>
          </IonCardContent>
        </IonCard>
      </IonContent>
    </IonPage>
  );
};

export default Tab2;
