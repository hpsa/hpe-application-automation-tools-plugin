package ut.com.hp.octane.plugins.bamboo.ui;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;

import com.hp.octane.plugins.bamboo.api.OctaneConfigurationKeys;
import com.hp.octane.plugins.bamboo.ui.ConfigureOctaneAction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureOctaneActionTest {

    @Mock
    PluginSettingsFactory settingsFactory;
    @Mock
    PluginSettings settings;

    @Captor
    ArgumentCaptor<String> propertyNameCaptor;

    @Captor
    ArgumentCaptor<String> valueCaptor;

    ConfigureOctaneAction underTest;

    String[] keys = new String[] { OctaneConfigurationKeys.OCTANE_URL, OctaneConfigurationKeys.ACCESS_KEY,
            OctaneConfigurationKeys.API_SECRET, OctaneConfigurationKeys.IMPERSONATION_USER};

    String[] values = new String[] { "url", "accessKey", "apiSecret","admin" };

    @Before
    public void setUp() {
        Mockito.when(settingsFactory.createGlobalSettings()).thenReturn(settings);
        underTest = new ConfigureOctaneAction(settingsFactory);
        
        underTest.setOctaneUrl(values[0]);
        underTest.setAccessKey(values[1]);
        underTest.setApiSecret(values[2]);
        underTest.setUserName(values[3]);
    }

    @Test
    public void testPropertiesLoaded() {

        underTest.doEdit();
        Mockito.verify(settings, Mockito.times(keys.length)).get(propertyNameCaptor.capture());
        Assert.assertArrayEquals(keys, propertyNameCaptor.getAllValues().toArray());
    }

    @Test
    @Ignore
    public void testPropertiesSaved() {
        underTest.doSave();
        Mockito.verify(settings, Mockito.times(keys.length)).put(propertyNameCaptor.capture(),
                valueCaptor.capture());
        Assert.assertArrayEquals(keys, propertyNameCaptor.getAllValues().toArray());
        Assert.assertArrayEquals(values, valueCaptor.getAllValues().toArray());
    }
}
