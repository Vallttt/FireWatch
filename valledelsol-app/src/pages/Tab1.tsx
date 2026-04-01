import {
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
  IonCard,
  IonCardHeader,
  IonCardSubtitle,
  IonCardTitle,
  IonCardContent,
  IonGrid,
  IonRow,
  IonCol,
  IonIcon,
  IonBadge
} from '@ionic/react';
import {
  warningOutline,
  flameOutline,
  shieldCheckmarkOutline
} from 'ionicons/icons';
import './Tab1.css';

const Tab1: React.FC = () => {
  return (
    <IonPage>
      <IonHeader>
        <IonToolbar color="danger">
          <IonTitle>Valle del Sol - Central</IonTitle>
        </IonToolbar>
      </IonHeader>

      <IonContent fullscreen color="light">
        <IonHeader collapse="condense">
          <IonToolbar color="danger">
            <IonTitle size="large">Valle del Sol</IonTitle>
          </IonToolbar>
        </IonHeader>

        {/* Resumen */}
        <div className="estado-container">
          <h2 className="estado-titulo">Estado Comunal</h2>

          <IonGrid>
            <IonRow>
              <IonCol size="6">
                <IonCard color="warning" className="card-resumen">
                  <IonCardHeader className="card-header-icon">
                    <IonIcon icon={warningOutline} className="icon-large" />
                  </IonCardHeader>
                  <IonCardContent>
                    <h2><strong>3</strong></h2>
                    <p>Alertas Activas</p>
                  </IonCardContent>
                </IonCard>
              </IonCol>

              <IonCol size="6">
                <IonCard color="danger" className="card-resumen">
                  <IonCardHeader className="card-header-icon">
                    <IonIcon icon={flameOutline} className="icon-large" />
                  </IonCardHeader>
                  <IonCardContent>
                    <h2><strong>1</strong></h2>
                    <p>Foco Detectado</p>
                  </IonCardContent>
                </IonCard>
              </IonCol>
            </IonRow>
          </IonGrid>
        </div>

        {/* Panel táctico */}
        <IonCard>
          <IonCardHeader>
            <IonCardSubtitle>Sector Plaza Central</IonCardSubtitle>
            <IonCardTitle>Reporte Crítico</IonCardTitle>
          </IonCardHeader>
          <IonCardContent>
            <IonBadge color="danger" className="badge-prioridad">
              PRIORIDAD ALTA
            </IonBadge>

            <p>
              Se reporta gran columna de humo cerca del parque.
              Equipos pre-desplegados.
            </p>

            <br />

            <img
              src="https://images.unsplash.com/photo-1599839619722-39751411ea63?q=80&w=2070&auto=format&fit=crop"
              alt="Incendio forestal"
              className="imagen-incendio"
            />
          </IonCardContent>
        </IonCard>

        {/* Brigadas */}
        <IonCard color="success">
          <IonCardHeader>
            <IonIcon
              icon={shieldCheckmarkOutline}
              className="brigada-icon"
            />
            <IonCardTitle>Brigadas Terrestres</IonCardTitle>
            <IonCardSubtitle>
              Unidades disponibles para despacho
            </IonCardSubtitle>
          </IonCardHeader>

          <IonCardContent>
            Actualmente contamos con <strong>4 cuadrillas</strong>
            listas en cuarteles para ser movilizadas por la subdirección.
          </IonCardContent>
        </IonCard>
      </IonContent>
    </IonPage>
  );
};

export default Tab1;