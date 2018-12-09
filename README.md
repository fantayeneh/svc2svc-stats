# Svc-2-Svc Stats
This Linkerd plugin exports the service to service call stats `/admin/metrics.json` 

Example stats for requests from service=A to service=B:
```
"rt/int/client/$/B/src/A/requests": 2877,
"rt/int/client/$/B/src/A/status/404": 2669,
"rt/int/client/$/B/src/A/status/4XX": 2669,
"rt/int/client/$/B/src/A/status/2XX": 205,
"rt/int/client/$/B/src/A/status/error": 3,
"rt/int/client/$/B/src/A/status/200": 205,
```

## Building

This plugin is built with sbt.  Run sbt from the plugins directory.

```
sbt assembly
```

This will produce the plugin jar at
`target/scala-2.12/svc2svc-stats-assembly-0.1.0-SNAPSHOT.jar`.

## Installing

To install this plugin with linkerd, simply move the plugin jar into linkerd's
plugin directory (`$L5D_HOME/plugins`).  Then add it in the requestAuthorizers block to the
router in your linkerd config:

```
routers:
  - protocol: http
    requestAuthorizers:
      - kind: com.homeaway.l5d.svc2SvcStatsRecorder
    dtab: /svc/* => /$/inet/10.0.1.9/8080
    httpAccessLog: logs/access.log
    label: int
    servers:
      - port: 4140
        ip: 0.0.0.0

```
