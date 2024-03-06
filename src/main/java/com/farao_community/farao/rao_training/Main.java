package com.farao_community.farao.rao_training;


import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.openrao.data.cracapi.Contingency;
import com.powsybl.openrao.data.cracapi.Crac;
import com.powsybl.openrao.data.cracapi.networkaction.NetworkAction;
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
        Crac crac = null;

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