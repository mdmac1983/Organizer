package com.mdmac.organizer.ui.apps

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.mdmac.organizer.R
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.data.home.HomeRepository
import com.mdmac.organizer.databinding.FragmentAppsBinding
import com.mdmac.organizer.profile.Profile
import com.mdmac.organizer.profile.ProfileManager
import com.mdmac.organizer.ui.settings.DrawerSettingsPreference
import kotlinx.coroutines.launch

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by viewModels {
        AppsViewModel.Factory(AppsRepository(requireContext()))
    }

    private lateinit var gridAdapter: AppGridAdapter
    private lateinit var drawerSettings: DrawerSettingsPreference
    private lateinit var homeRepository: HomeRepository
    private lateinit var profileManager: ProfileManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerSettings = DrawerSettingsPreference(requireContext())
        homeRepository = HomeRepository(requireContext())
        profileManager = ProfileManager(requireContext())

        gridAdapter = AppGridAdapter(
            onAppClick = ::launchApp,
            onAppLongClick = { app, anchorView -> showAppOptions(app, anchorView) },
            onHiddenFolderClick = ::showHiddenAppsDialog
        )
        binding.appsGridRecyclerView.layoutManager = GridLayoutManager(requireContext(), drawerSettings.getColumns())
        binding.appsGridRecyclerView.adapter = gridAdapter

        applyOpacityAndBrightness()

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.gridApps.collect { apps ->
                    submitGrid(apps)
                }
            }
        }
    }

    private fun applyOpacityAndBrightness() {
        val opacityAlpha = drawerSettings.getOpacity() / 100f
        binding.appsGridRecyclerView.alpha = opacityAlpha

        val transparency = drawerSettings.getBackgroundTransparency()
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true)
        val surfaceColor = typedValue.data
        val alphaChannel = (255 * (transparency / 100f)).toInt().coerceIn(0, 255)
        val blended = Color.argb(
            alphaChannel,
            Color.red(surfaceColor),
            Color.green(surfaceColor),
            Color.blue(surfaceColor)
        )
        binding.root.setBackgroundColor(blended)
    }

    /** Owner sees every non-hidden app; Guest is restricted to the separately-curated drawer list. */
    private fun submitGrid(apps: List<InstalledApp>) {
        val profile = profileManager.getCurrentProfile()
        val visibleApps = if (profile == Profile.GUEST) {
            val allowed = homeRepository.getGuestDrawerPackages()
            apps.filter { it.packageName in allowed }
        } else {
            apps
        }

        val hiddenCount = if (profile == Profile.OWNER) viewModel.hiddenApps.value.size else 0
        val items = mutableListOf<AppGridItem>()
        items.addAll(visibleApps.map { AppGridItem.AppEntry(it) })
        if (hiddenCount > 0) {
            items.add(AppGridItem.HiddenFolder(hiddenCount))
        }
        gridAdapter.submitList(items)
        binding.emptyView.visibility = if (visibleApps.isEmpty() && hiddenCount == 0) View.VISIBLE else View.GONE
    }

    private fun launchApp(app: InstalledApp) {
        val intent = requireContext().packageManager.getLaunchIntentForPackage(app.packageName)
        if (intent != null) {
            startActivity(intent)
        }
    }

    private fun showAppOptions(app: InstalledApp, anchorView: View): Boolean {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_app_options, popup.menu)
        popup.menu.findItem(R.id.action_pin_toggle).title =
            if (app.isPinned) "Remove from Dock" else "Pin to Dock"
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add_to_home -> {
                    val profile = profileManager.getCurrentProfile()
                    val current = homeRepository.getHomePackages(profile).toMutableSet()
                    current.add(app.packageName)
                    homeRepository.setHomePackages(profile, current)
                    Toast.makeText(requireContext(), "Added to Home", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_pin_toggle -> {
                    viewModel.togglePin(app)
                    true
                }
                R.id.action_app_info -> {
                    openAppInfo(app)
                    true
                }
                R.id.action_uninstall -> {
                    uninstallApp(app)
                    true
                }
                R.id.action_hide -> {
                    viewModel.setHidden(app, true)
                    true
                }
                else -> false
            }
        }
        popup.show()
        return true
    }

    private fun openAppInfo(app: InstalledApp) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", app.packageName, null)
        }
        startActivity(intent)
    }

    private fun uninstallApp(app: InstalledApp) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.fromParts("package", app.packageName, null)
        }
        startActivity(intent)
    }

    private fun showHiddenAppsDialog() {
        HiddenAppsDialog(viewModel.hiddenApps.value) { app ->
            viewModel.setHidden(app, false)
        }.show(childFragmentManager, "hidden_apps")
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
