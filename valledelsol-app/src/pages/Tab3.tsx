import { useState } from 'react';
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
  IonSelect,
  IonSelectOption,
  IonInput,
  IonTextarea,
  IonButton,
  IonIcon,
  IonToast,
  IonText
} from '@ionic/react';
import { paperPlaneOutline } from 'ionicons/icons';
import './Tab3.css';

const API_URL = "http://localhost:8000";

const Tab3: React.FC = () => {
  const [tipo, setTipo] = useState<string>('SMS');
  const [destinatario, setDestinatario] = useState<string>('');
  const [mensaje, setMensaje] = useState<string>('');
  const [showToast, setShowToast] = useState(false);
  const [errorToast, setErrorToast] = useState(false);
  const [loading, setLoading] = useState(false);

  const enviarAlerta = async () => {
    if (!destinatario.trim() || !mensaje.trim()) {
      setErrorToast(true);
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_URL}/api/alertas/enviar`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          tipoAlerta: tipo,
          destinatario,
          mensaje
        })
      });

      if (response.ok) {
        setShowToast(true);
        setDestinatario('');
        setMensaje('');
      } else {
        setErrorToast(true);
      }
    } catch (error) {
      setErrorToast(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar color="danger">
          <IonTitle>Despacho de Alertas</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent fullscreen color="light">
        <IonHeader collapse="condense">
          <IonToolbar color="danger">
            <IonTitle size="large">Alertas</IonTitle>
          </IonToolbar>
        </IonHeader>

        <IonCard className="alerta-card">
          <IonCardHeader>
            <IonCardTitle>Emitir Alerta a Ciudadanía</IonCardTitle>
            <IonText color="medium">
              <p>
                Esta alerta será procesada por el API Gateway
                y enviada al microservicio de alertas.
              </p>
            </IonText>
          </IonCardHeader>

          <IonCardContent>
            <IonItem className="form-item">
              <IonLabel position="stacked">
                Tipología de Alerta
              </IonLabel>
              <IonSelect
                value={tipo}
                onIonChange={(e) => setTipo(e.detail.value)}
              >
                <IonSelectOption value="SMS">
                  Notificación SMS
                </IonSelectOption>
                <IonSelectOption value="EMAIL">
                  Correo Electrónico
                </IonSelectOption>
                <IonSelectOption value="PUSH">
                  App Móvil (Push)
                </IonSelectOption>
              </IonSelect>
            </IonItem>

            <IonItem className="form-item">
              <IonLabel position="stacked">
                Destinatario
              </IonLabel>
              <IonInput
                placeholder="Ej. +56912345678"
                value={destinatario}
                onIonChange={(e) =>
                  setDestinatario(e.detail.value!)
                }
              />
            </IonItem>

            <IonItem className="form-item">
              <IonLabel position="stacked">
                Mensaje Oficial
              </IonLabel>
              <IonTextarea
                rows={4}
                placeholder="Escribe la alerta oficial..."
                value={mensaje}
                onIonChange={(e) =>
                  setMensaje(e.detail.value!)
                }
              />
            </IonItem>

            <IonButton
              expand="block"
              color="danger"
              size="large"
              className="btn-alerta"
              onClick={enviarAlerta}
              disabled={loading}
            >
              <IonIcon slot="end" icon={paperPlaneOutline} />
              {loading ? 'Transmitiendo...' : 'DISPARAR ALERTA'}
            </IonButton>
          </IonCardContent>
        </IonCard>

        <IonToast
          isOpen={showToast}
          onDidDismiss={() => setShowToast(false)}
          message={`¡Alerta ${tipo} enviada con éxito!`}
          duration={3000}
          color="success"
          position="bottom"
        />

        <IonToast
          isOpen={errorToast}
          onDidDismiss={() => setErrorToast(false)}
          message="Error al enviar la alerta o faltan campos."
          duration={3000}
          color="danger"
          position="bottom"
        />
      </IonContent>
    </IonPage>
  );
};

export default Tab3;