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
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdmac.organizer.R
import com.mdmac.organizer.data.apps.AppsRepository
import com.mdmac.organizer.data.apps.InstalledApp
import com.mdmac.organizer.databinding.FragmentAppsBinding
import com.mdmac.organizer.ui.settings.DrawerSettingsPreference
import com.mdmac.organizer.data.home.HomeRepository
import com.mdmac.organizer.profile.ProfileManager
import kotlinx.coroutines.launch

class AppsFragment : Fragment() {

    private var _binding: FragmentAppsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppsViewModel by viewModels {
        AppsViewModel.Factory(AppsRepository(requireContext()))
    }

    private lateinit var gridAdapter: AppGridAdapter
    private lateinit var dockAdapter: AppGridAdapter
    private lateinit var drawerSettings: DrawerSettingsPreference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerSettings = DrawerSettingsPreference(requireContext())

        gridAdapter = AppGridAdapter(
            onAppClick = ::launchApp,
            onAppLongClick = { app, anchorView -> showAppOptions(app, anchorView) },
            onHiddenFolderClick = ::showHiddenAppsDialog
        )
        binding.appsGridRecyclerView.layoutManager = GridLayoutManager(requireContext(), drawerSettings.getColumns())
        binding.appsGridRecyclerView.adapter = gridAdapter

        dockAdapter = AppGridAdapter(
            onAppClick = ::launchApp,
            onAppLongClick = { app, anchorView -> showAppOptions(app, anchorView) },
            onHiddenFolderClick = {}
        )
        binding.dockRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.dockRecyclerView.adapter = dockAdapter

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
                launch {
                    viewModel.gridApps.collect { apps ->
                        submitGrid(apps)
                    }
                }
                launch {
                    viewModel.dockApps.collect { dockApps ->
                        binding.dockRecyclerView.visibility =
                            if (dockApps.isEmpty()) View.GONE else View.VISIBLE
                        dockAdapter.submitList(dockApps.map { AppGridItem.AppEntry(it) })
                    }
                }
            }
        }
    }

    /**
     * Icon opacity dims the grid+dock content; brightness lightens/darkens icons
     * (currently a stored value only, no visible effect yet — flagged in Batch C
     * as needing a per-icon color filter pass); background transparency blends
     * the theme's surface color down toward fully see-through, so the Home
     * wallpaper shows through behind the drawer as it's dragged down.
     */
    private fun applyOpacityAndBrightness() {
        val opacityAlpha = drawerSettings.getOpacity() / 100f
        binding.appsGridRecyclerView.alpha = opacityAlpha
        binding.dockRecyclerView.alpha = opacityAlpha

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

    private fun submitGrid(apps: List<InstalledApp>) {
        val hiddenCount = viewModel.hiddenApps.value.size
        val items = mutableListOf<AppGridItem>()
        items.addAll(apps.map { AppGridItem.AppEntry(it) })
        if (hiddenCount > 0) {
            items.add(AppGridItem.HiddenFolder(hiddenCount))
        }
        gridAdapter.submitList(items)
        binding.emptyView.visibility = if (apps.isEmpty() && hiddenCount == 0) View.VISIBLE else View.GONE
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
