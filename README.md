# WorldVIP

## Comandos
| Comando               | Descrição                                   |
|-----------------------|---------------------------------------------|
| /vip                  | Abrir menu principal                        |
| /vip top              | Mostrar o vip top                           |
| /vip itens            | Configurar itens de ativação do vip         |
| /vip reload           | Recarregar configurações                    |
| /vip ajuda            | Lista de comandos                           |
| /vip removerpendentes | Remover vips pendents de um jogador         |
| /tempovip             | Visualizar o tempo restante do vip primario |
| /trocarvip            | Remover vip ou vips de um jogador           |
| /darvip               | Dar vip para um jogador                     |
| /removervip           | Remover vip ou vips de um jogador           |
| /setarvip             | Remover vip ou vips de um jogador           |
| /criarkey             | Criar uma key vip                           |
| /removerkey           | Remover uma key existente                   |
| /verkeys              | Ver keys proprias ou de outro jogador       |
| /usarkey              | Usar uma key existente e ativar um vip      |

## Permissões
| Permissão                 | Descrição                            |
|---------------------------|--------------------------------------|
| worldvip.setitems         | Executar /vip itens                  |
| worldvip.reload           | Executar /vip reload                 |
| worldvip.ajudastaff       | Mostrar lista de comandos para staff |
| worldvip.removerpendentes | Executar /vip removerpendentes       |
| worldvip.tempovip         | Executar /tempovip <jogador>         |
| worldvip.darvip           | Executar /darvip                     |
| worldvip.removervip       | Executar /removervip                 |
| worldvip.setarvip         | Executar /setarvip                   |
| worldvip.criarkey         | Executar /criarkey                   |
| worldvip.removerkey       | Executar /removerkey                 |
| worldvip.verkeys          | Executar /verkeys <jogador>          |

## Configuração
| Arquivo/pasta  | Função                                                                                                                              |
|----------------|-------------------------------------------------------------------------------------------------------------------------------------|
| config.yml     | Configurações gerais comod database, geração da key, opções sobre o vip como stackar permissões, reduzir tempo simultâneamente, etc |
| vip.yml        | Configurar vips (nome, preço, display, etc)                                                                                         |
| resposta/      | Pasta de configuração de respostas como mensagens, sons e efeitos                                                                   |
| menu/          | Pasta de configuração de menus                                                                                                      |
| itens_data.yml | Itens de ativação dos vips. Não modificar diretamente                                                                               |
| server.yml     | Aproximação do ultimo instante online do servidor (uso interno). Não modificar diretamente                                          |
