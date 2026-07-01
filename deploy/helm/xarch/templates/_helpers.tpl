{{/*
Expand the name of the chart.
*/}}
{{- define "xarch.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to
this (by the DNS naming spec).
*/}}
{{- define "xarch.fullname" -}}
{{- if .Values.nameOverride -}}
{{- .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Chart name and version label.
*/}}
{{- define "xarch.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels applied to every resource.
*/}}
{{- define "xarch.labels" -}}
helm.sh/chart: {{ include "xarch.chart" . }}
{{ include "xarch.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: xarch
{{- end -}}

{{/*
Selector labels (subset of xarch.labels used as matchLabels).
*/}}
{{- define "xarch.selectorLabels" -}}
app.kubernetes.io/name: {{ include "xarch.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Return the proper image name.
Usage:
  {{ include "xarch.image" ( dict "image" .Values.path.to.image "global" .Values.global "context" . ) }}
*/}}
{{- define "xarch.image" -}}
{{- $registry := "" -}}
{{- if .global.imageRegistry -}}
  {{- $registry = printf "%s/" .global.imageRegistry -}}
{{- end -}}
{{- printf "%s%s:%s" $registry .image.repository (.image.tag | default "latest") -}}
{{- end -}}

{{/*
Resolve resource block for a workload based on preset or override.
Usage:
  {{ include "xarch.resources" ( dict "preset" .Values.microservices.resourcePreset "presets" .Values.presets "override" .Values.serviceAuthResources ) }}
*/}}
{{- define "xarch.resources" -}}
{{- $preset := dig "requests" "" (dig .preset nil .presets) -}}
{{- $base := dig .preset nil .presets -}}
{{- if .override -}}
{{- toYaml .override -}}
{{- else if $base -}}
{{- toYaml $base -}}
{{- else -}}
{}
{{- end -}}
{{- end -}}

{{/*
Common service account name used by sub-charts.
*/}}
{{- define "xarch.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (printf "%s-sa" (include "xarch.fullname" .)) .Values.serviceAccount.name -}}
{{- else -}}
{{- default "default" .Values.serviceAccount.name -}}
{{- end -}}
{{- end -}}

{{/*
Render a Secret body from key/value pairs. Sensitive data should never be
embedded directly in values.yaml; values must reference existing Secrets via
secretKeyRef. This helper exists to keep template code DRY.
*/}}
{{- define "xarch.toYamlMap" -}}
{{- range $k, $v := . -}}
{{ $k }}: {{ $v | quote }}
{{- end -}}
{{- end -}}

{{/*
Build the list of image pull secrets in the right format.
*/}}
{{- define "xarch.imagePullSecrets" -}}
{{- if .Values.global.imagePullSecrets -}}
{{- toYaml .Values.global.imagePullSecrets -}}
{{- end -}}
{{- end -}}
