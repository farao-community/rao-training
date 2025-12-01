package com.farao_community.farao.rao_training;


import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlow;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.nad.NetworkAreaDiagram;
import com.powsybl.openrao.data.crac.api.Crac;
import com.powsybl.openrao.data.crac.api.networkaction.NetworkAction;
import com.powsybl.openrao.data.raoresult.api.RaoResult;
import com.powsybl.openrao.raoapi.Rao;
import com.powsybl.openrao.raoapi.RaoInput;
import com.powsybl.openrao.raoapi.parameters.ObjectiveFunctionParameters;
import com.powsybl.openrao.raoapi.parameters.RaoParameters;
import com.powsybl.openrao.raoapi.parameters.extensions.OpenRaoSearchTreeParameters;

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
        OpenRaoSearchTreeParameters searchTreeParameters = new OpenRaoSearchTreeParameters();
        searchTreeParameters.getLoadFlowAndSensitivityParameters().getSensitivityWithLoadFlowParameters().getLoadFlowParameters().setDc(true);
        RaoParameters raoParameters = new RaoParameters();
        raoParameters.addExtension(OpenRaoSearchTreeParameters.class, searchTreeParameters);
        raoParameters.getObjectiveFunctionParameters().setType(ObjectiveFunctionParameters.ObjectiveFunctionType.MAX_MIN_MARGIN);
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
        contingency.ifPresent(value -> value.toModification().apply(network));
        networkActions.forEach(na -> na.apply(network));
        LoadFlowParameters loadFlowParameters = new LoadFlowParameters();
        loadFlowParameters.setDc(true);
        LoadFlow.find("OpenLoadFlow").run(network);
        NetworkAreaDiagram.draw(network, Path.of(path));
        network.getVariantManager().setWorkingVariant(initialVariant);
        network.getVariantManager().removeVariant("tmp");
    }
}