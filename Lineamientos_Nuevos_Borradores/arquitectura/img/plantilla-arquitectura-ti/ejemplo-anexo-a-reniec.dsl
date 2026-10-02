// Fuente de los diagramas de ejemplo del Anexo A de Plantilla_Arquitectura_TI_ONP_v1.4.md
// (sistema de ejemplo: Carga y Consulta de Datos RENIEC).
// Para regenerar los PNG (requiere Java):
//   structurizr-cli export -w ejemplo-anexo-a-reniec.dsl -f plantuml/structurizr -o out
//   java -jar plantuml.jar -tpng out/*.puml
// y copiar out/structurizr-A_*.png sobre los ejemplo-a*.png de esta carpeta.

workspace "Carga y Consulta de Datos RENIEC" "Vistas de Arquitectura" {

    model {
        appONP = softwareSystem "Aplicaciones internas ONP" "Aplicaciones y sistemas legados de la ONP que consultan datos de personas" "Existente"

        group "Entidades Externas - RENIEC" {
            reniecWS = softwareSystem "RENIEC - Servicio Web" "Consulta de datos RUIPN por DNI (SOAP)" "Externo"
            reniecFiles = softwareSystem "RENIEC - Servicio de Archivos" "Archivos de Carga Cero y Novedades (Actualizacion, Fallecidos)" "Externo"
        }
        mail = softwareSystem "Servicio institucional de correo" "SMTP institucional (existente)" "Externo"
        obs = softwareSystem "Plataforma de Observabilidad" "Logs, metricas y trazas (existente)" "Externo"
        saa = softwareSystem "SAA - Autenticacion y Autorizacion" "Emite y valida tokens tecnicos (existente)" "Externo"

        sistema = softwareSystem "Sistema de Carga y Consulta de Datos RENIEC" "Mantiene el padron de personas sincronizado con RENIEC" {
            gateway = container "API Manager / Gateway" "Perimetro de exposicion institucional" "Existente" "Existente"

            ws = container "WS-CONSULTA-RENIEC" "Expone consulta cache-aside general y operacion dedicada para el ETL" "Java / REST" {
                wsSec = component "Filtro de Seguridad" "Valida el token contra SAA y la autorizacion de la aplicacion para la operacion invocada" "Security Filter"
                wsCtrl = component "Controlador REST" "Recibe la operacion general y la operacion dedicada" "REST Controller"
                wsValid = component "Validador de Input" "Valida el DNI recibido" "Application Service"
                wsSvc = component "Servicio de Consulta" "Op. general: cache local y alta por cache-miss, previa validacion con el catalogo AD-009. Op. dedicada: solo consulta a RENIEC, sin escribir" "Application Service"
                wsRepo = component "Repositorio MAE_PERSONA" "Lee, y crea solo por cache-miss de la op. general (nunca actualiza)" "Repository"
                wsClient = component "RENIEC Client / Adapter" "Encapsula la llamada SOAP a RENIEC, clasifica el codigo de resultado y aplica la resiliencia (AD-012)" "SOAP Client"
                wsAudit = component "Servicio de Auditoria" "Registra toda solicitud, sin excepcion, en AUD_CONSULTA_RENIEC" "Application Service"
            }

            etl = container "SICAR-ETL-RENIEC" "Carga Cero, Actualizacion diaria y Fallecidos" "Pentaho Data Integration 9.1" {
                etlCtl = component "Job de Control" "J_CTL - orquesta los tres flujos (Carga Cero una vez; luego Actualizacion -> Fallecidos en secuencia) y notifica el resultado" "ETL Job"
                etlExt = component "Transformation de Extraccion" "Copia el archivo plano al esquema temporal TRX_" "ETL Transformation"
                etlVal = component "Transformation de Validacion" "Aplica el catalogo unico de reglas (AD-009) al archivo y a las respuestas de RENIEC; desvia rechazos a cuarentena" "ETL Transformation"
                etlBulk = component "Carga Masiva (Carga Cero)" "INSERT directo y paralelo a MAE_PERSONA vacia, con conciliacion (AD-007)" "ETL Transformation"
                etlClient = component "RENIEC Client / Adapter" "Invoca la operacion dedicada de WS-CONSULTA-RENIEC via API Manager / Gateway, con reintentos (Actualizacion, y Fallecidos para DNI inexistentes)" "ETL Transformation (REST Client)"
                etlCmp = component "Componente de Comparacion y MERGE" "Compara campo a campo contra MAE_PERSONA; cada flujo escribe solo sus columnas (AD-010)" "ETL Transformation"
            }

            db = container "Oracle - Esquema (Por definir)" "MAE_PERSONA, HIS_PERSONA, HIS_DIRECCION, TRX_RENIEC_*, ERR_TRX_RENIEC_*" "Oracle 19c" "Database"

            bdAudit = container "BD de Auditoria" "AUD_CONSULTA_RENIEC - una fila por solicitud a WS-CONSULTA-RENIEC, sin excepcion" "Oracle 19c" "Database"
        }

        # Relaciones a nivel contenedor / sistema
        appONP -> gateway "Consulta datos de personas" "REST/HTTPS"
        gateway -> wsSec "Enruta, con token Bearer" "REST/HTTPS"
        ws -> db "Lee, y crea por cache-miss"
        ws -> reniecWS "Consulta (cache-miss en operacion general, siempre en operacion dedicada)" "SOAP/HTTPS (TLS 1.2)"
        etl -> db "CRUD"
        etl -> mail "Notifica resultado de cada ejecucion" "SMTP"
        reniecFiles -> etl "Deposita archivos Carga Cero / Novedades" "SFTP"
        ws -> obs "Telemetria (logs, metricas, trazas)"
        gateway -> obs "Logs / metricas / trazas" "async"
        etl -> obs "Logs / metricas / trazas (pendiente confirmar conectividad)" "async"
        gateway -> saa "Solicita token"

        # Relaciones a nivel componente (dentro de WS-CONSULTA-RENIEC)
        wsSec -> saa "Valida token" "REST/HTTPS"
        wsSec -> wsCtrl "Solicitud autorizada"
        wsSec -> wsAudit "Registra solicitud rechazada"
        wsCtrl -> wsValid "Valida input"
        wsValid -> wsSvc "Input validado"
        wsValid -> wsAudit "Registra input invalido"
        wsSvc -> wsRepo "Consulta; crea solo por cache-miss (op. general)"
        wsSvc -> wsClient "Cache-miss o siempre (operacion dedicada)"
        wsSvc -> wsAudit "Registra resultado, siempre"
        wsClient -> reniecWS "Metodo consultar, con usuario y contrasena (AD-013)" "SOAP/HTTPS (TLS 1.2)"
        wsRepo -> db "Lee / crea"
        wsAudit -> bdAudit "Registra cada solicitud"

        wsSec -> obs "Logs / metricas / trazas" "async"
        wsCtrl -> obs "Logs / metricas / trazas" "async"
        wsSvc -> obs "Logs / metricas / trazas" "async"
        wsClient -> obs "Logs / metricas / trazas" "async"

        # Relaciones a nivel componente (dentro de SICAR-ETL-RENIEC)
        reniecFiles -> etlCtl "Deposita archivos Carga Cero / Novedades" "SFTP"
        # El Job de Control orquesta; las transformations no se invocan entre si,
        # intercambian datos a traves de las tablas TRX_ de Oracle.
        etlCtl -> etlExt "1. Extrae (Carga Cero, Actualizacion, Fallecidos)"
        etlCtl -> etlVal "2. Valida archivos y respuesta de RENIEC"
        etlCtl -> etlBulk "3a. Carga masiva (solo Carga Cero)"
        etlCtl -> etlClient "3b. Consulta RENIEC (Actualizacion; Fallecidos solo DNI inexistentes)"
        etlCtl -> etlCmp "4. Compara y MERGE (Actualizacion y Fallecidos)"
        etlCtl -> mail "Notifica resultado de cada ejecucion" "SMTP"
        etlExt -> db "Escribe archivo en TRX_RENIEC_*"
        etlVal -> db "Lee TRX_RENIEC_*; rechazos a ERR_TRX_RENIEC_*"
        etlBulk -> db "Lee TRX_ validos; INSERT directo en MAE_PERSONA"
        etlClient -> gateway "REST sincrono, operacion dedicada, con reintentos" "REST/HTTPS"
        etlClient -> db "Lee DNIs y cuarentena; guarda respuestas en TRX_RENIEC_*; fallos a ERR_"
        etlCmp -> db "Lee TRX_ validos; archiva en HIS_*; MERGE en MAE_PERSONA"

        deploymentEnvironment "Produccion" {
            deploymentNode "Zona / Servicio de intercambio de Archivos" {
                sftpNode = infrastructureNode "Carpeta SFTP Aplicativos RENIEC"
            }

            deploymentNode "Red Interna de Aplicaciones" {
                deploymentNode "Servidor / VM Pentaho Server (excepcion EXC-001)" "Pentaho Server 9.1 Community" {
                    etlInstance = containerInstance etl
                }
                deploymentNode "Kubernetes WS-CONSULTA-RENIEC" "Java / REST" {
                    wsInstance = containerInstance ws
                }
                deploymentNode "API Manager / Gateway" {
                    gatewayInstance = containerInstance gateway
                }
            }

            deploymentNode "Red de Datos" {
                deploymentNode "Oracle - Esquema (Por definir)" {
                    dbInstance = containerInstance db
                }
                deploymentNode "Oracle - Esquema de Auditoria (Por definir)" {
                    bdAuditInstance = containerInstance bdAudit
                }
            }

            deploymentNode "Red / Servicios Externos" {
                reniecWSInstance = softwareSystemInstance reniecWS
            }

            deploymentNode "Servicios Transversales" {
                mailInstance = softwareSystemInstance mail
                obsInstance = softwareSystemInstance obs
                saaInstance = softwareSystemInstance saa
            }

            sftpNode -> etlInstance "SFTP / lectura"
        }
    }

    views {
        systemContext sistema "A_1" "Sistema de Carga y Consulta de Datos RENIEC" {
            include *
            title "A.1 Vista de Contexto"
        }

        container sistema "A_2" "Sistema de Carga y Consulta de Datos RENIEC" {
            include *
            title "A.2 Vista de Aplicacion"
        }

        component etl "A_3_1" "Sistema de Carga y Consulta de Datos RENIEC" {
            include *
            include ws
            title "A.3.1 Vista de Componentes de SICAR-ETL-RENIEC"
        }

        component ws "A_3_2" "Sistema de Carga y Consulta de Datos RENIEC" {
            include *
            include saa
            title "A.3.2 Vista de Componentes de WS-CONSULTA-RENIEC"
        }

        container sistema "A_4_1" "Sistema de Carga y Consulta de Datos RENIEC" {
            include appONP
            include gateway
            include ws
            include etl
            include mail
            include reniecWS
            include reniecFiles
            include saa
            title "A.4.1 Vista de Integraciones - Funcional"
        }

        container sistema "A_4_2" "Sistema de Carga y Consulta de Datos RENIEC" {
            include gateway
            include ws
            include etl
            include obs
            title "A.4.2 Vista de Integraciones - Observabilidad"
        }

        deployment sistema "Produccion" "A_5" "Sistema de Carga y Consulta de Datos RENIEC" {
            include *
            title "A.5 Vista de Infraestructura"
        }

        styles {
            element "Person" {
                shape Person
                background #08427b
                color #ffffff
            }
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "Externo" {
                background #999999
                color #ffffff
            }
            element "Existente" {
                background #85bbf0
                color #000000
            }
            element "Database" {
                shape Cylinder
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
            element "Component" {
                background #85bbf0
                color #000000
            }
        }
    }
}
