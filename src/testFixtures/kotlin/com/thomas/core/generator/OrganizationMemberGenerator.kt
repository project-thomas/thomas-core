package com.thomas.core.generator

import com.thomas.core.data.OrganizationTestData
import com.thomas.core.data.UnitTestData
import com.thomas.core.generator.RoleGenerator.generateOrganizationRoles
import com.thomas.core.generator.RoleGenerator.generateUnitRoles
import com.thomas.core.model.security.SecurityOrganization
import com.thomas.core.model.security.SecurityUnit
import java.util.UUID
import kotlin.random.Random

object OrganizationUnitGenerator {

    private val namesDescriptions = mapOf(
        "TechNova" to listOf("Inovação em software e IA.", null),
        "CodeSphere" to listOf("Desenvolvimento global de software.", null),
        "QuantumSoft" to listOf("Software avançado quântico.", null),
        "ByteWorks" to listOf("Soluções tecnológicas criativas.", null),
        "CyberCore" to listOf("Segurança cibernética de ponta.", null),
        "NeuraLink" to listOf("Inteligência artificial conectada.", null),
        "CloudShift" to listOf("Serviços de computação em nuvem.", null),
        "InfoZen" to listOf("Gestão de dados simplificada.", null),
        "PixelForge" to listOf("Design e desenvolvimento digital.", null),
        "NextGen Solutions" to listOf("Inovação tecnológica futura.", null),
        "DataPulse" to listOf("Análise de big data eficiente.", null),
        "SmartWave" to listOf("Soluções tecnológicas inteligentes.", null),
        "InnoTech" to listOf("Tecnologia de inovação disruptiva.", null),
        "HexaByte" to listOf("Soluções de dados escaláveis.", null),
        "FusionNet" to listOf("Conectividade e rede de dados.", null),
        "Skybound Technologies" to listOf("Tecnologias de nuvem e IA.", null),
        "VortexCode" to listOf("Desenvolvimento de software ágil.", null),
        "Cortex Systems" to listOf("Sistemas de aprendizado profundo.", null),
        "ZetaStream" to listOf("Processamento de dados em tempo real.", null),
        "BinaryEdge" to listOf("Segurança de redes digitais.", null),
        "VirtuTech" to listOf("Soluções de realidade virtual.", null),
        "AlphaMatrix" to listOf("Inteligência computacional avançada.", null),
        "DigitalFusion" to listOf("Integração digital eficiente.", null),
        "BlueCircuit" to listOf("Engenharia eletrônica inovadora.", null),
        "CodePulse Innovations" to listOf("Software rápido e dinâmico.", null),
        "NovaCore" to listOf("Núcleo de soluções de TI.", null),
        "OptiTech Solutions" to listOf("Otimização de sistemas tecnológicos.", null),
        "NeuroSphere" to listOf("Tecnologias neurocognitivas avançadas.", null),
        "CloudBridge" to listOf("Plataforma de integração em nuvem.", null),
        "Synapse Tech" to listOf("Conexão entre IA e dados.", null),
        "DeepMindware" to listOf("Desenvolvimento de IA avançada.", null),
        "HyperWave Technologies" to listOf("Tecnologias de ponta e velocidade.", null),
        "StratoNet" to listOf("Rede de dados e computação em nuvem.", null),
        "GridLogic" to listOf("Soluções lógicas em grade.", null),
        "QuantumNet" to listOf("Redes quânticas e soluções digitais.", null),
        "EchoByte" to listOf("Comunicação digital avançada.", null),
        "NanoCore Innovations" to listOf("Tecnologia em nanoescala.", null),
        "OpticWave" to listOf("Comunicação óptica de alta velocidade.", null),
        "ByteShift Solutions" to listOf("Soluções de dados ágeis.", null),
        "TechHive" to listOf("Colmeia de inovação tecnológica.", null),
        "XenonTech" to listOf("Soluções de TI revolucionárias.", null),
        "LogicStream" to listOf("Fluxo de dados inteligente.", null),
        "CyberPulse" to listOf("Monitoramento cibernético seguro.", null),
        "EvoSoft" to listOf("Evolução contínua de software.", null),
        "Neon Systems" to listOf("Sistemas digitais brilhantes.", null),
        "EdgeLink" to listOf("Conexão de ponta a ponta.", null),
        "NexaLogic" to listOf("Soluções lógicas de próxima geração.", null),
        "CircuitTree" to listOf("Engenharia de circuitos inteligentes.", null),
        "CloudCore Innovations" to listOf("Núcleo de tecnologias em nuvem.", null),
        "InfoMatrix" to listOf("Gestão e análise de informações.", null),
        "ProtoLogic" to listOf("Prototipagem rápida de software.", null),
        "TechBridge" to listOf("Conectando inovação e TI.", null),
        "NanoTek" to listOf("Soluções tecnológicas em nanoescala.", null),
        "ByteWave" to listOf("Fluxo de dados e software.", null),
        "CyberSphere" to listOf("Segurança digital global.", null),
        "SkyCode" to listOf("Desenvolvimento de software na nuvem.", null),
        "PixelNet" to listOf("Rede de desenvolvimento digital.", null),
        "DataCore" to listOf("Núcleo de soluções de big data.", null),
        "TechPulse" to listOf("Monitoramento de tecnologia em tempo real.", null),
        "AlphaWave" to listOf("Tecnologia de comunicação avançada.", null),
        "InfoPulse" to listOf("Processamento inteligente de informações.", null),
        "MegaSoft" to listOf("Soluções escaláveis de software.", null),
        "TechFusion" to listOf("Integração de múltiplas plataformas.", null),
        "NeuraCode" to listOf("Codificação baseada em IA.", null),
        "CoreWave" to listOf("Soluções de TI centrais e ágeis.", null),
        "DataBridge" to listOf("Conectividade de dados simplificada.", null),
        "SynapseLink" to listOf("Conexões neuronais em IA.", null),
        "HexaNet" to listOf("Rede de dados multidimensional.", null),
        "QuantumLink" to listOf("Comunicação e processamento quântico.", null),
        "OpticCore" to listOf("Núcleo de sistemas de fibra óptica.", null),
        "CortexLink" to listOf("Sistemas conectados baseados em IA.", null),
        "CyberStream" to listOf("Fluxo seguro de dados digitais.", null),
        "NeuraLogic" to listOf("Soluções cognitivas e lógicas.", null),
        "EchoTech" to listOf("Tecnologias de eco digital.", null),
        "BitFusion" to listOf("Integração de bits e dados.", null),
        "TechMind" to listOf("Inteligência computacional e AI.", null),
        "CloudEdge" to listOf("Computação na nuvem de última geração.", null),
        "HyperLogic" to listOf("Lógica hiper-rápida em TI.", null),
        "QuantumFlow" to listOf("Fluxo de dados quântico e rápido.", null),
        "BlueNet" to listOf("Rede de dados e comunicação avançada.", null),
        "DataForge" to listOf("Criação e processamento de dados.", null),
        "CoreShift" to listOf("Mudança dinâmica em sistemas centrais.", null),
        "InfoLink" to listOf("Conexão de informações em rede.", null),
        "VirtualSync" to listOf("Sincronização de realidade virtual.", null),
        "SmartGrid" to listOf("Rede inteligente e eficiente.", null),
        "ByteCore" to listOf("Núcleo de sistemas de dados rápidos.", null),
        "TechFlow" to listOf("Fluxo contínuo de inovação tecnológica.", null),
        "NovaLink" to listOf("Conexão e comunicação inovadora.", null),
        "CloudStream" to listOf("Transmissão e processamento em nuvem.", null),
        "InfoSync" to listOf("Sincronização de dados e informações.", null),
        "BitCore" to listOf("Soluções centrais de bits e dados.", null),
        "SynapseCore" to listOf("Núcleo de sistemas neurais e IA.", null),
        "CyberBridge" to listOf("Conectividade e segurança digital.", null),
        "PixelLink" to listOf("Conexão digital para design e tecnologia.", null),
        "QuantumForge" to listOf("Criação e processamento quântico.", null),
        "OptiNet" to listOf("Rede de otimização de sistemas.", null),
        "NeuroNet" to listOf("Rede neural baseada em IA.", null),
        "EchoStream" to listOf("Fluxo contínuo de dados e eco digital.", null),
        "CloudFusion" to listOf("Fusão de plataformas de nuvem.", null),
        "NexaCore" to listOf("Soluções centrais de próxima geração.", null),
    )

    @Suppress("MagicNumber")
    fun generateOrganization(): OrganizationTestData = namesDescriptions.entries.random().let {
        OrganizationTestData(
            id = UUID.randomUUID(),
            organizationName = "${it.key} - ${Random.nextInt(1000, 9999)}",
            organizationDescription = it.value.random(),
            isActive = listOf(true, false).random(),
        )
    }

    fun generateSecurityOrganization(): SecurityOrganization = generateOrganization().let {
        SecurityOrganization(
            organizationId = it.id,
            organizationName = it.organizationName,
            organizationRoles = generateOrganizationRoles(),
        )
    }

    @Suppress("MagicNumber")
    fun generateUnit(): UnitTestData = namesDescriptions.entries.random().let {
        UnitTestData(
            id = UUID.randomUUID(),
            unitName = "${it.key} - ${Random.nextInt(1000, 9999)}",
            unitDescription = it.value.random(),
            isActive = listOf(true, false).random(),
        )
    }

    fun generateSecurityUnit(): SecurityUnit = generateUnit().let {
        SecurityUnit(
            unitId = it.id,
            unitName = it.unitName,
            unitRoles = generateUnitRoles(),
        )
    }

    fun generateSecurityUnitSet(
        quantity: Int = 3
    ): Set<SecurityUnit> = (1..quantity).map {
        generateSecurityUnit()
    }.toSet()

}
