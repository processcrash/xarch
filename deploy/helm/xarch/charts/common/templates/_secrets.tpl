{{/*
Build a Secret object that aggregates per-service credentials.
Usage:
  {{ include "common.appSecret" ( dict "name" "xarch-secrets" "values" .Values.commonSecrets ) }}
*/}}
{{- define "common.appSecret" -}}
{{- $data := dict -}}
{{- range $k, $v := .values -}}
{{- if and $v (not (empty $v)) -}}
{{- $_ := set $data $k ($v | b64enc | quote) -}}
{{- end -}}
{{- end -}}
{{- if $data }}
apiVersion: v1
kind: Secret
metadata:
  name: {{ .name }}
  namespace: {{ $.Release.Namespace | default "xarch" }}
  labels:
    {{- include "xarch.labels" $ | nindent 4 }}
type: Opaque
data:
{{- range $k, $v := $data }}
  {{ $k }}: {{ $v }}
{{- end }}
{{- end }}
{{- end -}}

{{/*
Render envFrom list pulling from a Secret name.
*/}}
{{- define "common.envFromSecret" -}}
- secretRef:
    name: {{ . }}
{{- end -}}
