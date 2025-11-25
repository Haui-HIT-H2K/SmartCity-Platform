export type AlertType = "fire" | "traffic" | "aqi" | "other";

export interface Alert {
  id: string;
  type: AlertType;
  lat: number;
  lon: number;
  timestamp: string;
  description: string;
  source: string;
}

export interface DataMetrics {
  throughput: number[];
  breakdown: {
    hot: number;
    warm: number;
    cold: number;
  };
  timestamp: number;
}

export interface WebSocketMessage {
  type: "alert" | "metrics" | "alert_resolved";
  data: Alert | DataMetrics | { id: string };
}
