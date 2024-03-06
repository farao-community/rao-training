package com.farao_community.farao.rao_training;

import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.openrao.commons.Unit;
import com.powsybl.openrao.data.cracapi.Contingency;
import com.powsybl.openrao.data.cracapi.Crac;
import com.powsybl.openrao.data.cracapi.CracFactory;
import com.powsybl.openrao.data.cracapi.InstantKind;
import com.powsybl.openrao.data.cracapi.cnec.Side;
import com.powsybl.openrao.data.cracapi.networkaction.ActionType;
import com.powsybl.openrao.data.cracapi.networkaction.NetworkAction;
import com.powsybl.openrao.data.cracapi.usagerule.UsageMethod;
import com.powsybl.openrao.data.raoresultapi.RaoResult;
import com.powsybl.openrao.raoapi.Rao;
import com.powsybl.openrao.raoapi.RaoInput;
import com.powsybl.openrao.raoapi.parameters.ObjectiveFunctionParameters;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        runSimpleRao();
    }

    private static void runSimpleRao() {
        Network network = Network.read("simple_network.uct", Main.class.getResourceAsStream("/simple_network.uct"));

        // Add your code here
        Crac crac = CracFactory.findDefault().create("nom");

        // CRAC instants
        crac.newInstant("preventive", InstantKind.PREVENTIVE)
            .newInstant("outage", InstantKind.OUTAGE)
            .newInstant("curative", InstantKind.CURATIVE);

        // Preventive CNEC & RA
        crac.newFlowCnec()
            .withId("nl2_be3_1_preventive")
            .withNetworkElement("NNL2AA11 BBE3AA11 1")
            .withInstant("preventive")
            .newThreshold().withSide(Side.LEFT).withMax(500.).withMin(-500.).withUnit(Unit.MEGAWATT).add()
            .withOptimized()
            .add();

        crac.newNetworkAction()
            .withId("close_nl2_be3_2")
            .newTopologicalAction().withNetworkElement("NNL2AA11 BBE3AA11 2").withActionType(ActionType.CLOSE).add()
            .newOnInstantUsageRule().withInstant("preventive").withUsageMethod(UsageMethod.AVAILABLE).add()
            .add();

        // N-1 "co1" : contingency, curative CNEC & CRA
        crac.newContingency()
            .withId("co1_loss_of_one_line")
            .withNetworkElement("FFR2AA11 NNL3AA11 1")
            .add();

        crac.newFlowCnec()
            .withId("nl2_be3_1_co1_curative")
            .withNetworkElement("NNL2AA11 BBE3AA11 1")
            .withInstant("curative")
            .withContingency("co1_loss_of_one_line")
            .newThreshold().withSide(Side.LEFT).withMax(500.).withMin(-500.).withUnit(Unit.MEGAWATT).add()
            .withOptimized()
            .add();

        crac.newNetworkAction()
            .withId("close_nl2_be3_3")
            .newTopologicalAction().withNetworkElement("NNL2AA11 BBE3AA11 3").withActionType(ActionType.CLOSE).add()
            .newOnInstantUsageRule().withInstant("curative").withUsageMethod(UsageMethod.AVAILABLE).add()
            .add();

        // N-2 "co2" : contingency, curative CNEC & CRA
        crac.newContingency()
            .withId("co2_loss_of_two_lines")
            .withNetworkElement("FFR2AA11 NNL3AA11 1")
            .withNetworkElement("FFR1AA11 FFR3AA11 1")
            .add();

        crac.newFlowCnec()
            .withId("nl2_be3_1_co2_curative")
            .withNetworkElement("NNL2AA11 BBE3AA11 1")
            .withInstant("curative")
            .withContingency("co2_loss_of_two_lines")
            .newThreshold().withSide(Side.LEFT).withMax(500.).withMin(-500.).withUnit(Unit.MEGAWATT).add()
            .withOptimized()
            .add();

        crac.newFlowCnec()
            .withId("fr3_fr2_1_co2_curative")
            .withNetworkElement("FFR2AA11 FFR3AA11 1")
            .withInstant("curative")
            .withContingency("co2_loss_of_two_lines")
            .newThreshold().withSide(Side.LEFT).withMax(1200.).withMin(-1200.).withUnit(Unit.MEGAWATT).add()
            .withOptimized()
            .add();

        crac.newNetworkAction()
            .withId("increase_fr1_production")
            .newInjectionSetPoint().withNetworkElement("FFR1AA11_generator").withSetpoint(-1200.).withUnit(Unit.MEGAWATT).add()
            .newInjectionSetPoint().withNetworkElement("NNL1AA11_generator").withSetpoint(1200.).withUnit(Unit.MEGAWATT).add()
            .newOnInstantUsageRule().withInstant("curative").withUsageMethod(UsageMethod.AVAILABLE).add()
            .add();

        // Optional (needs ORTOOLS): use injection range action instead of injection setpoint
        /*crac.newInjectionRangeAction()
            .withId("increase_fr1_production")
            .withNetworkElementAndKey(-1.0, "FFR1AA11_generator")
            .withNetworkElementAndKey(1.0, "NNL1AA11_generator")
            .newRange().withMin(1200.).withMax(1800.).add()
            .newOnInstantUsageRule().withInstant("curative").withUsageMethod(UsageMethod.AVAILABLE).add()
            .add();*/

        RaoInput raoInput = RaoInput.build(network, crac).build();
        RaoParameters raoParameters = getRaoParameters();
        RaoResult raoResult = Rao.find().run(raoInput, raoParameters);
    }

    private static RaoParameters getRaoParameters() {
        RaoParameters raoParameters = new RaoParameters();
        raoParameters.getLoadFlowAndSensitivityParameters().getSensitivityWithLoadFlowParameters().getLoadFlowParameters().setDc(true);
        raoParameters.getObjectiveFunctionParameters().setPreventiveStopCriterion(ObjectiveFunctionParameters.PreventiveStopCriterion.MIN_OBJECTIVE);
        raoParameters.getObjectiveFunctionParameters().setCurativeStopCriterion(ObjectiveFunctionParameters.CurativeStopCriterion.MIN_OBJECTIVE);
        return raoParameters;
    }

    private static void drawNetwork(Network network, String path) {
        LoadFlowParameters loadFlowParameters = new LoadFlowParameters();
        loadFlowParameters.setDc(true);
        LoadFlow.find("OpenLoadFlow").run(network);
        NetworkAreaDiagram.draw(network, Path.of(path));
    }

    private static void drawNetwork(Network network, Optional<Contingency> contingency, Set<NetworkAction> networkActions, String path) {
        String initialVariant = network.getVariantManager().getWorkingVariantId();
        network.getVariantManager().cloneVariant(initialVariant, "tmp");
        network.getVariantManager().setWorkingVariant("tmp");
        contingency.ifPresent(value -> value.apply(network, null));
        networkActions.forEach(na -> na.apply(network));
        LoadFlowParameters loadFlowParameters = new LoadFlowParameters();
        loadFlowParameters.setDc(true);
        LoadFlow.find("OpenLoadFlow").run(network);
        NetworkAreaDiagram.draw(network, Path.of(path));
        network.getVariantManager().setWorkingVariant(initialVariant);
        network.getVariantManager().removeVariant("tmp");
    }
}