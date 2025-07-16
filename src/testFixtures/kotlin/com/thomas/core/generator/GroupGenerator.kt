package com.thomas.core.generator

import com.thomas.core.data.GroupTestData
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityOrganization
import com.thomas.core.generator.OrganizationUnitGenerator.generateSecurityUnitSet
import com.thomas.core.model.security.SecurityGroup
import java.util.UUID
import kotlin.random.Random

object GroupGenerator {

    private val groupNamesDescriptions = mapOf(
        "Administração" to listOf("Grupo do departamento de administração", null),
        "Recursos Humanos" to listOf("Setor dos recursos humanos", null),
        "Financeiro" to listOf("Departamento financeiro da empresa", null),
        "Contábil" to listOf("Setor contábil", null),
        "Marketing" to listOf("Grupo de marketing", null),
        "Vendas" to listOf("Setor de venda da produção da empresa", null),
        "Produção" to listOf("Produção de items da empresa", null),
        "Logística" to listOf("Departamento de logística", null),
        "Tecnologia da Informação" to listOf("Departamento de tecnologia da informação e afins", null),
        "Jurídico" to listOf("Setor legal e jurídico", null),
        "Pesquisa" to listOf("Setor de pesquisas empresariais", null),
        "Compras" to listOf("Departamento de compras externas", null),
        "Suprimentos" to listOf("Departamento de compras internas", null),
        "Atendimento ao Cliente" to listOf("SAC do cliente", null),
        "Manutenção" to listOf("Departamento de manutenções", null),
        "Atendimento ao Colaborador" to listOf("Grupo de apoio ao Colaborador", null),
        "Relações Públicas" to listOf("Relação da Empresa com o Público", null),
        "Sistema de Atendimento ao Consumidor" to listOf("S.A.C. da empresa", null),
        "Setor Comercial" to listOf("Setor Geral Comercial", null),
        "Setor de Segurança" to listOf("Setor de Acessos e Monitoria", null),
        "Diretoria" to listOf("Diretores da Empresa", null),
        "Gestores Tecnógicos" to listOf("Gestores de Pessoas de TI", null),
        "Gestores Comerciais" to listOf("Gestores de Pessoas do Comercial", null),
        "Gestores de Vendas" to listOf("Gestores de Pessoas dos Vendedores", null),
        "Gerentes Regionais Norte" to listOf("Gerentes da Região Norte", null),
        "Gerentes Regionais Nordeste" to listOf("Gerentes da Região Nordeste", null),
        "Gerentes Regionais Centro-Oeste" to listOf("Gerentes da Região Centro-Oeste", null),
        "Gerentes Regionais Sudeste" to listOf("Gerentes da Região Sudeste", null),
        "Gerentes Regionais Sul" to listOf("Gerentes da Região Sul", null),
        "Pesquisa de Mercado" to listOf("Setor de Pesquisa de Mercado", null),
        "Pesquisas Técnológicas" to listOf("Setor de Pesquisa de Novas Técnologias", null),
        "Pesquisa de Novos Produtos" to listOf("Setor de Pesquisa de Novos Produtos", null),
        "Pesquisa de Satisfação" to listOf("Setor de Pesquisa de Satisfação do Cliente", null),
        "Setor Operacional" to listOf("Setor de Operações da Empresa", null),
        "Operações Internas" to listOf("Operações Internas da Empresa", null),
        "Operações Externas" to listOf("Operações Externas da Empresa", null),
        "Operações Especiais" to listOf("Operações Especiais da Empresa", null),
        "Cobranças" to listOf("Área de Cobranças de Pagamentos", null),
        "Gestão de Recursos Internos" to listOf("Gestão dos Recusos Usados Internamente", null),
        "Gestão de Recursos Externos" to listOf("Gestão dos Recusos Usados Externamente", null),
        "Contábil e Fiscal" to listOf("Setor de Contabilidade Fiscal", null),
        "Funcionários" to listOf("Grupo Geral dos Funcionários", null),
        "Gestão Ambiental" to listOf("Gestão Ambiental dos Recursos", null),
        "Execução Orçamentária" to listOf("Execução dos Orçamentos Aprovados", null),
        "Avaliadores de Processo" to listOf("Avaliação dos Processos Executados", null),
    )

    @Suppress("MagicNumber")
    fun generateGroup(): GroupTestData = groupNamesDescriptions.entries.random().let {
        GroupTestData(
            id = UUID.randomUUID(),
            groupName = "${it.key} - ${Random.nextInt(1000, 9999)}",
            groupDescription = it.value.random(),
            isActive = listOf(true, false).random(),
        )
    }

    fun generateSecurityGroup(): SecurityGroup = generateGroup().let {
        SecurityGroup(
            groupId = it.id,
            groupName = it.groupName,
            securityOrganization = generateSecurityOrganization(),
            securityUnits = generateSecurityUnitSet(),
        )
    }

    fun generateSecurityGroupSet(
        quantity: Int = 3,
    ): Set<SecurityGroup> = (1..quantity).map {
        generateSecurityGroup()
    }.toSet()

}
